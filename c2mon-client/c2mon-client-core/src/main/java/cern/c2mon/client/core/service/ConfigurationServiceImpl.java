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
package cern.c2mon.client.core.service;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.ConfigurationService;
import cern.c2mon.client.core.configuration.*;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.alarm.AlarmCondition;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.client.configuration.api.tag.Tag;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import java.util.*;

@Service("configurationService")
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

  /**
   * Provides methods for requesting tag information from the C2MON server
   */
  private RequestHandler clientRequestHandler;

  private ConfigurationRequestSender configurationRequestSender;

  private ProcessConfigurationManager processConfigurationManager;

  private EquipmentConfigurationManager equipmentConfigurationManager;

  private SubEquipmentConfigurationManager subEquipmentConfigurationManager;

  private DataTagConfigurationManager dataTagConfigurationManager;

  private RuleTagConfigurationManager ruleTagConfigurationManager;

  private AlarmConfigurationManager alarmConfigurationManager;

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   *
   * @param requestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected ConfigurationServiceImpl(final @Qualifier("coreRequestHandler") RequestHandler requestHandler, final ConfigurationRequestSender configurationRequestSender,
                                     ProcessConfigurationManager processConfigurationManager, EquipmentConfigurationManager equipmentConfigurationManager, SubEquipmentConfigurationManager subEquipmentConfigurationManager,
                                     DataTagConfigurationManager dataTagConfigurationManager, RuleTagConfigurationManager ruleTagConfigurationManager, AlarmConfigurationManager alarmConfigurationManager) {
    this.clientRequestHandler = requestHandler;
    this.configurationRequestSender = configurationRequestSender;
    this.processConfigurationManager = processConfigurationManager;
    this.equipmentConfigurationManager = equipmentConfigurationManager;
    this.subEquipmentConfigurationManager = subEquipmentConfigurationManager;
    this.dataTagConfigurationManager = dataTagConfigurationManager;
    this.ruleTagConfigurationManager = ruleTagConfigurationManager;
    this.alarmConfigurationManager = alarmConfigurationManager;
  }

  @Override
  public ConfigurationReport applyConfiguration(final Long configurationId) {
    return clientRequestHandler.applyConfiguration(configurationId);
  }

  @Override
  public ConfigurationReport applyConfiguration(Long configurationId, ClientRequestReportListener reportListener) {
    return clientRequestHandler.applyConfiguration(configurationId, reportListener);
  }

  @Override
  public ConfigurationReport applyConfiguration(Configuration configuration, ClientRequestReportListener listener) {
    return configurationRequestSender.applyConfiguration(configuration, listener);
  }

  @Override
  public Collection<ConfigurationReportHeader> getConfigurationReports() {
    try {
      return clientRequestHandler.getConfigurationReports();
    } catch (JMSException e) {
      log.error("getConfigurationReports() - JMS connection lost -> Could not retrieve configuration reports from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<ConfigurationReport> getConfigurationReports(Long id) {
    try {
      return clientRequestHandler.getConfigurationReports(id);
    } catch (JMSException e) {
      log.error("getConfigurationReports() - JMS connection lost -> Could not retrieve configuration reports from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<ProcessNameResponse> getProcessNames() {

    try {
      return clientRequestHandler.getProcessNames();
    } catch (JMSException e) {
      log.error("getProcessNames() - JMS connection lost -> Could not retrieve process names from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<TagConfig> getTagConfigurations(final Collection<Long> tagIds) {

    try {
      // no cache for Tag Configurations => fetch them from the server
      return clientRequestHandler.requestTagConfigurations(tagIds);
    } catch (JMSException e) {
      log.error("getTagConfigurations() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public String getProcessXml(final String processName) {

    try {
      return clientRequestHandler.getProcessXml(processName);
    } catch (JMSException e) {
      log.error("getProcessXml() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return null;
  }

  public ConfigurationReport createProcess(String processName) {
    return processConfigurationManager.createProcess(processName);
  }

  public ConfigurationReport createProcess(Process process) {
    return processConfigurationManager.createProcess(process);
  }

  @Override
  public ConfigurationReport updateProcess(Process process) {
    return processConfigurationManager.updateProcess(process);
  }

  @Override
  public ConfigurationReport removeProcessById(Long id) {
    return processConfigurationManager.removeProcessById(id);
  }

  @Override
  public ConfigurationReport removeProcess(String name) {
    return processConfigurationManager.removeProcess(name);
  }

  @Override
  public ConfigurationReport createEquipment(String processName, String name, String handlerClass) {
    return equipmentConfigurationManager.createEquipment(processName, name, handlerClass);
  }

  @Override
  public ConfigurationReport createEquipment(String processName, Equipment equipment) {
    return equipmentConfigurationManager.createEquipment(processName, equipment);
  }

  @Override
  public ConfigurationReport createEquipment(String processName, List<Equipment> equipments) {
    return equipmentConfigurationManager.createEquipment(processName, equipments);
  }

  @Override
  public ConfigurationReport updateEquipment(Equipment equipment) {
    return equipmentConfigurationManager.updateEquipment(equipment);
  }

  @Override
  public ConfigurationReport updateEquipment(List<Equipment> equipments) {
    return equipmentConfigurationManager.updateEquipment(equipments);
  }

  @Override
  public ConfigurationReport removeEquipmentById(Long id) {
    return equipmentConfigurationManager.removeEquipmentById(id);
  }

  @Override
  public ConfigurationReport removeEquipmentById(Set<Long> ids) {
    return equipmentConfigurationManager.removeEquipmentById(ids);
  }

  @Override
  public ConfigurationReport removeEquipment(String equipmentName) {
    return equipmentConfigurationManager.removeEquipment(equipmentName);
  }

  @Override
  public ConfigurationReport removeEquipment(Set<String> equipmentNames) {
    return equipmentConfigurationManager.removeEquipment(equipmentNames);
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, String name, String handlerClass) {
    return subEquipmentConfigurationManager.createSubEquipment(equipmentName, name, handlerClass);
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, SubEquipment subEquipment) {
    return subEquipmentConfigurationManager.createSubEquipment(equipmentName, subEquipment);
  }

  @Override
  public ConfigurationReport createSubEquipment(String equipmentName, List<SubEquipment> subEquipments) {
    return subEquipmentConfigurationManager.createSubEquipment(equipmentName, subEquipments);
  }

  @Override
  public ConfigurationReport updateSubEquipment(SubEquipment subEquipment) {
    return subEquipmentConfigurationManager.updateSubEquipment(subEquipment);
  }

  @Override
  public ConfigurationReport updateSubEquipment(List<SubEquipment> subEquipments) {
    return subEquipmentConfigurationManager.updateSubEquipment(subEquipments);
  }

  @Override
  public ConfigurationReport removeSubEquipmentById(Long id) {
    return subEquipmentConfigurationManager.removeSubEquipmentById(id);
  }

  @Override
  public ConfigurationReport removeSubEquipmentById(Set<Long> ids) {
    return subEquipmentConfigurationManager.removeSubEquipmentById(ids);
  }

  @Override
  public ConfigurationReport removeSubEquipment(String subEquipmentName) {
    return subEquipmentConfigurationManager.removeSubEquipment(subEquipmentName);
  }

  @Override
  public ConfigurationReport removeSubEquipment(Set<String> subEquipmentNames) {
    return subEquipmentConfigurationManager.removeSubEquipment(subEquipmentNames);
  }

  @Override
  public ConfigurationReport createDataTag(String equipmentName, String name, Class<?> dataType, DataTagAddress address) {
    return dataTagConfigurationManager.createDataTag(equipmentName, name, dataType, address);
  }

  @Override
  public ConfigurationReport createDataTag(String equipmentName, DataTag dataTag) {
    return dataTagConfigurationManager.createDataTag(equipmentName, dataTag);
  }

  @Override
  public ConfigurationReport createDataTags(String equipmentName, List<DataTag> tags) {
    return dataTagConfigurationManager.createDataTags(equipmentName, tags);
  }

  @Override
  public ConfigurationReport updateTag(Tag tag) {
    return dataTagConfigurationManager.updateTag(tag);
  }

  @Override
  public ConfigurationReport updateTags(List<Tag> tags) {
    return dataTagConfigurationManager.updateTags(tags);
  }

  @Override
  public ConfigurationReport removeTagsById(Set<Long> ids) {
    return dataTagConfigurationManager.removeTagsById(ids);
  }

  @Override
  public ConfigurationReport removeTagById(Long id) {
    return dataTagConfigurationManager.removeTagById(id);
  }

  @Override
  public ConfigurationReport removeTag(String name) {
    return dataTagConfigurationManager.removeTag(name);
  }

  @Override
  public ConfigurationReport removeTags(Set<String> tagNames) {
    return dataTagConfigurationManager.removeTags(tagNames);
  }

  @Override
  public ConfigurationReport createRule(String ruleExpression, String name, Class<?> dataType) {
    return ruleTagConfigurationManager.createRule(ruleExpression, name, dataType);
  }

  @Override
  public ConfigurationReport createRule(RuleTag createRuleTag) {
    return ruleTagConfigurationManager.createRule(createRuleTag);
  }

  @Override
  public ConfigurationReport createRules(List<RuleTag> ruleTags) {
    return ruleTagConfigurationManager.createRules(ruleTags);
  }

  @Override
  public ConfigurationReport createAlarm(String tagName, AlarmCondition alarmCondition, String faultFamily, String faultMember, Integer faultCode) {
    return alarmConfigurationManager.createAlarm(tagName, alarmCondition, faultFamily, faultMember, faultCode);
  }

  @Override
  public ConfigurationReport createAlarm(String tagName, Alarm alarm) {
    return alarmConfigurationManager.createAlarm(tagName, alarm);
  }

  @Override
  public ConfigurationReport createAlarms(Map<String, Alarm> alarms) {
    return alarmConfigurationManager.createAlarms(alarms);
  }

  @Override
  public ConfigurationReport updateAlarm(Alarm updateAlarm) {
    return alarmConfigurationManager.updateAlarm(updateAlarm);
  }

  @Override
  public ConfigurationReport updateAlarms(List<Alarm> alarms) {
    return alarmConfigurationManager.updateAlarms(alarms);
  }

  @Override
  public ConfigurationReport removeAlarm(Long id) {
    return alarmConfigurationManager.removeAlarm(id);
  }

  @Override
  public ConfigurationReport removeAlarms(Set<Long> ids) {
    return alarmConfigurationManager.removeAlarms(ids);
  }
}