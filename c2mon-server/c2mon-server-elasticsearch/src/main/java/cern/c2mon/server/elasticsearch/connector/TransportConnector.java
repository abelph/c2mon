/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.elasticsearch.connector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;
import cern.c2mon.server.elasticsearch.structure.types.EsSupervisionEvent;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Allows to connect to the cluster via a transport client. Handles all the
 * queries and writes data thanks to a bulkProcessor for {@link EsTag}
 * or 1 by 1 for  {@link EsAlarm} and {@link EsSupervisionEvent} to the Elasticsearch cluster.
 * This is very light for the cluster to be connected this way.
 *
 * @author Alban Marguet
 */
@Slf4j
@Service
public class TransportConnector {

  /**
   * Name of the BulkProcessor.
   */
  private final static String bulkProcessorName = "ES-BulkProcessor";

  @Autowired
  private ElasticsearchProperties properties;

  /**
   * The Client communicates with the Node inside the Elasticsearch cluster.
   */
  @Getter @Setter
  private Client client;

  /**
   * Allows to send the data by batch.
   */
  @Getter @Setter
  private BulkProcessor bulkProcessor;

  /**
   * True if connected to Elasticsearch
   */
  @Getter
  private boolean isConnected;

  @PostConstruct
  public void init() {
    client = createClient();

    if (properties.isEmbedded()) {
      launchLocalCluster();
    }

    connectAsynchronously();
  }

  /**
   * Creates a {@link Client} to communicate with the Elasticsearch cluster.
   *
   * @return the {@link Client} instance
   */
  private Client createClient() {
    final Settings.Builder settingsBuilder = Settings.settingsBuilder();

    settingsBuilder.put("node.name", properties.getNodeName())
            .put("cluster.name", properties.getClusterName())
            .put("http.enabled", properties.isHttpEnabled());

    log.debug("Creating client {} at {}:{} in cluster {}",
        properties.getNodeName(), properties.getHost(), properties.getPort(), properties.getClusterName());
    TransportClient client = TransportClient.builder().settings(settingsBuilder.build()).build();

    try {
      client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(properties.getHost()), properties.getPort()));
    } catch(UnknownHostException e) {
      log.error("Error connecting to the Elasticsearch cluster at {}:{}", properties.getHost(), properties.getPort(), e);
      return null;
    }

    return client;
  }

  /**
   * Connect to the cluster in a separate thread.
   */
  private void connectAsynchronously() {
    log.info("Trying to connect to Elasticsearch cluster {} at {}:{}",
        properties.getClusterName(), properties.getHost(), properties.getPort());

    new Thread(() -> {
      waitForYellowStatus();

      log.info("Connected to Elasticsearch cluster {}", properties.getClusterName());
      initBulkProcessor();

    }, "EsClusterFinder").start();
  }

  /**
   * Block and wait for the cluster to become yellow.
   */
  public void waitForYellowStatus() {
    while (!isConnected) {
      log.debug("Waiting for yellow status of Elasticsearch cluster...");

      try {
        ClusterHealthStatus status = getClusterHealth().getStatus();
        if (status.equals(ClusterHealthStatus.YELLOW) || status.equals(ClusterHealthStatus.GREEN)) {
          isConnected = true;
          break;
        }
      } catch(Exception e) {
        log.warn("Elasticsearch cluster not yet ready: {}", e.getMessage());
      }

      try {
        Thread.sleep(100L);
      } catch (InterruptedException ignored) {}
    }

    log.debug("Elasticsearch cluster is yellow");
  }

  private ClusterHealthResponse getClusterHealth() {
    return client.admin().cluster().prepareHealth()
        .setWaitForYellowStatus()
        .setTimeout(TimeValue.timeValueMillis(100))
        .get();
  }

  /**
   * Instantiate a BulkProcessor for batch operations.
   */
  private void initBulkProcessor() {
    log.debug("Creating BulkProcessor");
    this.bulkProcessor = BulkProcessor.builder(client, createBulkProcessorListener())
            .setName(bulkProcessorName)
            .setBulkActions(properties.getBulkActions())
            .setBulkSize(new ByteSizeValue(properties.getBulkSize(), ByteSizeUnit.MB))
            .setFlushInterval(TimeValue.timeValueSeconds(properties.getBulkFlushInterval()))
            .setConcurrentRequests(properties.getConcurrentRequests())
            .build();
  }

  /**
   * Creates a new {@link BulkProcessor.Listener}.
   *
   * @return {@link BulkProcessor.Listener} instance
   */
  private BulkProcessor.Listener createBulkProcessorListener() {
    return new BulkProcessor.Listener() {
      @Override
      public void beforeBulk(long executionId, BulkRequest request) {
        log.debug("Going to execute new bulk operation composed of {} actions", request.numberOfActions());
      }

      @Override
      public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
        log.debug("Executed bulk operation composed of {} actions", request.numberOfActions());
        waitForYellowStatus();
        refreshIndices();
      }

      @Override
      public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
        log.warn("Error executing bulk operation", failure);
        waitForYellowStatus();
      }
    };
  }

  /**
   * Add an indexRequest to the BulkProcessor in order to write data to Elasticsearch.
   */
  public boolean bulkAdd(IndexRequest request) {
    Assert.notNull(bulkProcessor, "BulkProcessor must not be null!");
    Assert.notNull(request, "IndexRequest must not be null!");

    bulkProcessor.add(request);
    return true;
  }

  public boolean createIndexTypeMapping(String index, String type, String mapping) {
    if (client == null) {
      log.error("Elasticsearch connection not yet initialized");
      return false;
    }

    PutMappingResponse response = null;
    try {
      response = client.admin().indices().preparePutMapping(index).setType(type).setSource(mapping).get();
    } catch (Exception e) {
      log.error("Error occurred whilst preparing the mapping for index={}, type={}, mapping={}", index, type, mapping, e);
    }

    return response != null && response.isAcknowledged();
  }

  /**
   * @return the set of indices present in Elasticsearch
   */
  public Set<String> retrieveIndices() {
    if (client == null) {
      return Collections.emptySet();
    }

    log.trace("Updating list of indices");
    return getListOfIndicesFromES().stream()
            .distinct()
            .collect(Collectors.toSet());
  }

  /**
   * @return the set of types associated to the index {@param index} in Elasticsearch
   */
  public Set<String> retrieveTypes(String index) {
    if (client == null) {
      return Collections.emptySet();
    }

    log.trace("Updating list of types");
    return getTypesFromES(index).stream()
            .distinct()
            .collect(Collectors.toSet());
  }

  public void refreshIndices() {
    client.admin().indices().prepareRefresh().execute().actionGet();
  }

  /**
   * Launch a local (to the JVM) Elasticsearch cluster that will answer to the Queries.
   *
   * @return Node with which we can communicate
   */
  private Node launchLocalCluster() {
    log.info("Launching an embedded Elasticsearch cluster: {}", properties.getClusterName());

    return nodeBuilder().settings(Settings.settingsBuilder()
        .put("path.home", properties.getEmbeddedStoragePath())
        .put("cluster.name", properties.getClusterName())
        .put("node.local", false)
        .put("node.name", properties.getNodeName())
        .put("node.data", true)
        .put("node.master", true)
        .put("network.host", "0.0.0.0")
        .put("http.enabled", true)
        .put("http.cors.enabled", true)
        .put("http.cors.allow-origin", "/.*/")
        .build()).node();
  }

  /**
   * Method called to close the newly opened client.
   *
   * @param client {@link Client} for the cluster.
   */
  public void close(Client client) {
    if (client != null) {
      client.close();
      log.info("Closed client {}", client.settings().get("node.name"));
    }
  }

  /**
   * Retrieve all the indices in Elasticsearch as a String array.
   */
  protected List<String> getIndicesFromCluster() {
    String[] indices = client.admin().indices().prepareGetIndex().get().indices();
    return Arrays.asList(indices);
  }

  /**
   * Query the cluster in order to do an Index operation.
   */
  protected CreateIndexRequestBuilder prepareCreateIndexRequestBuilder(String index) {
    CreateIndexRequestBuilder builder = client.admin().indices().prepareCreate(index);

    Settings indexSettings = Settings.settingsBuilder()
        .put("number_of_shards", properties.getShardsPerIndex())
        .put("number_of_replicas", properties.getReplicasPerShard())
        .build();
    builder.setSettings(indexSettings);

    return builder;
  }

  /**
   * Query the cluster in order to check, whether the given index is existing
   */
  private boolean isIndexExisting(String index) {
    ActionFuture<IndicesExistsResponse> response = client.admin().indices().exists(new IndicesExistsRequest(index));

    return response.actionGet(1000L).isExists();
  }

  /**
   * Allows to retrieve all the mappings inside a cluster.
   */
  protected Iterator<ObjectCursor<IndexMetaData>> getIndicesWithMetadata() {
    return client.admin().cluster().prepareState().get().getState().getMetaData().indices().values().iterator();
  }

  /**
   * Allows to retrieve the mappings of an index in Elasticsearch.
   */
  protected ImmutableOpenMap<String, MappingMetaData> getIndexWithMetadata(String index) {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().index(index).getMappings();
  }

  /**
   * @return true if the specified index exists.
   */
  protected boolean indexExists(String indexName) {
    if (Strings.isNullOrEmpty(indexName)) {
      return false;
    }

    log.debug("Checking if index {} exists" + indexName);

    // TODO: cache this...
    return client.admin().indices().prepareExists(indexName).get().isExists();
  }

  /**
   * Write a new {@link EsAlarm} to Elasticsearch.
   *
   * @param indexName to contain the data.
   * @param mapping   as JSON.
   * @param esAlarm   to be written.
   * @return true if the cluster has acknowledged the query.
   */
  public boolean logAlarmEvent(String indexName, String mapping, EsAlarm esAlarm) {
    if (client == null) {
      log.error("Elasticsearch connection not yet initialized");
      return false;
    }

    log.debug("Try to write new alarm event to index {}", indexName);

    String jsonSource = esAlarm.toString();
    String routing = String.valueOf(esAlarm.getId());

    boolean indexExists = true;
    if (!indexExists(indexName)) {
      log.debug("Creating new alarm index {}", indexName);
      indexExists = prepareCreateIndexRequestBuilder(indexName).setSource(mapping).get().isAcknowledged();
    }

    if (indexExists) {
      log.debug("Adding new alarm event to index {}", indexName);
      return client.prepareIndex().setIndex(indexName)
                   .setType("alarm")
                   .setSource(jsonSource)
                   .setRouting(routing)
                   .get().isCreated();
    }

    return false;
  }

  /**
   * Write a new {@link EsSupervisionEvent} to Elasticsearch.
   *
   * @param indexName          to contain the data.
   * @param mapping            as JSON.
   * @param esSupervisionEvent to be written.
   * @return true if the cluster has acknowledged the query.
   */
  public boolean logSupervisionEvent(String indexName, String mapping, EsSupervisionEvent esSupervisionEvent) {
    if (client == null) {
      log.error("Elasticsearch connection not yet initialized");
      return false;
    }

    log.debug("Trying to write new supervision event to index {}", indexName);

    String jsonSource = esSupervisionEvent.toString();
    String routing = esSupervisionEvent.getId();

    boolean indexExists = true;
    if (!indexExists(indexName)) {
      log.debug("Creating new supervision index {}", indexName);
      indexExists = prepareCreateIndexRequestBuilder(indexName).setSource(mapping).get().isAcknowledged();
    }

    if (indexExists) {
      log.debug("Adding new supervision event to index {}", indexName);
      return client.prepareIndex().setIndex(indexName)
                   .setType("supervision")
                   .setSource(jsonSource)
                   .setRouting(routing)
                   .get().isCreated();
    }

    return false;
  }

  /**
   * Simple query to get all the indices in the cluster.
   *
   * @return names of the indices.
   */
  public List<String> getListOfIndicesFromES() {
    if (client == null) {
      log.warn("Client is null!");
      return Collections.emptyList();
    }

    List<String> indicesFromCluster = getIndicesFromCluster();
    log.debug("Got a list of {} indices", indicesFromCluster.size());

    return indicesFromCluster;
  }

  /**
   * Simple query to get all the types of the {@param index}.
   */
  public Collection<String> getTypesFromES(String index) {
    if (index == null) {
      return Collections.emptySet();
    }
    final Collection<String> types = Sets.newHashSet(getIndexWithMetadata(index).keysIt());

    log.info("Got a list of {} types", types.size());
    return types;
  }

  public boolean createIndex(String indexName) {
    if (client == null) {
      log.error("Elasticsearch connection not yet initialized");
      return false;
    }

    if (isIndexExisting(indexName)) {
      return true;
    }

    CreateIndexRequestBuilder createIndexRequestBuilder = prepareCreateIndexRequestBuilder(indexName);
    try {
      CreateIndexResponse response = createIndexRequestBuilder.get();
      return response.isAcknowledged();
    } catch (IndexAlreadyExistsException ex) {
      return true;
    }
  }
}
