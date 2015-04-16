/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2005-2011 CERN.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.jms;

import java.util.Collection;

import javax.jms.JMSException;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.command.CommandExecuteRequest;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandTagHandle;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.shared.client.statistics.TagStatisticsResponse;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * Interface to Spring singleton bean proving convenient server
 * request methods.
 *
 * @author Mark Brightwell
 *
 */
public interface RequestHandler {


  /**
   * Applies the configuration and returns a Configuration Report.
   * The values are fetched from the server.
   * However, in case of a connection error or an unknown configuration Id the corresponding
   * tag might be missing.
   *
   * @param configurationId The configuration id used to fetch the Configuration Report object
   * @return A Configuration Report object
   */
    ConfigurationReport applyConfiguration(Long configurationId);

    /**
     * Applies the configuration and returns a Configuration Report.
     * The values are fetched from the server.
     * However, in case of a connection error or an unknown configuration Id the corresponding
     * tag might be missing.
     *
     * @param configurationId The configuration id used to fetch the Configuration Report object
     * @param reportListener Is informed about the progress of the operation on the server side.
     * @see ClientRequestProgressReport
     * @see ClientRequestErrorReport
     * @return A Configuration Report object
     */
    ConfigurationReport applyConfiguration(Long configurationId, ClientRequestReportListener reportListener);

    /**
     * Queries the server for the latest values and configuration
     * details for the request tags.
     *
     * <p>If called with an empty collection returns an empty collection.
     *
     * @param tagIds the ids of the tags
     * @return a collection of Alarms ({@link AlarmValue})
     * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
     * @throws NullPointerException if called with a null argument
     * @throws RuntimeException if the response from the server is null (probable timeout)
     */
    Collection<AlarmValue> requestAlarms(Collection<Long> tagIds) throws JMSException;

    /**
     * Queries the server for a collection of the (latest) active alarms.
     *
     * @return a collection of active alarms ({@link AlarmValue})
     * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
     * @throws NullPointerException if called with a null argument
     * @throws RuntimeException if the response from the server is null (probable timeout)
     */
    Collection<AlarmValue> requestAllActiveAlarms() throws JMSException;

    /**
     * Queries the server for the latest values and configuration
     * details for the request tags.
     *
     * <p>If called with an empty collection returns an empty collection.
     *
     * @param tagIds the ids of the tags
     * @return a collection of TagConfigurations
     * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
     * @throws NullPointerException if called with a null argument
     * @throws RuntimeException if the response from the server is null (probable timeout)
     */
    Collection<TagConfig> requestTagConfigurations(Collection<Long> tagIds) throws JMSException;


  /**
   * Queries the server for the latest values and configuration
   * details for the request tags.
   *
   * <p>If called with an empty collection returns an empty collection.
   *
   * @param tagIds the ids of the tags
   * @return a collection of transfer objects with the values/configuration information
   * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
   * @throws NullPointerException if called with a null argument
   * @throws RuntimeException if the response from the server is null (probable timeout)
   */
  Collection<TagUpdate> requestTags(Collection<Long> tagIds) throws JMSException;

  /**
   * Queries the server for the latest values for the request tags.
   *
   * <p>If called with an empty collection returns an empty collection.
   *
   * @param tagIds the ids of the tags
   * @return a collection of transfer objects with the value information
   * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
   * @throws NullPointerException if called with a null argument
   * @throws RuntimeException if the response from the server is null (probable timeout)
   */
  Collection<TagValueUpdate> requestTagValues(Collection<Long> tagIds) throws JMSException;

  /**
   * Queries the server for statistics about the number of configured and invalid tags.
   *
   * @return a {@link TagStatisticsResponse} object contining the tag statistics
   * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
   */
  TagStatisticsResponse requestTagStatistics() throws JMSException;

  /**
   * Queries the server for the current Supervision status of all
   * entities in the server (Process, Equipment and SubEquipment).
   * @return a collection of current events, each containing the status
   *                  of one of the entities
   * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
   * @throws RuntimeException if the response from the server is null (probable timeout)
   */
  Collection<SupervisionEvent> getCurrentSupervisionStatus() throws JMSException;

  /**
   * Request CommandTags from the server.
   * @param commandIds ids of desired command tags
   * @return a collection of command handle objects
   * @throws JMSException if a JMS problems occurs or if not connected at the moment
   * @throws RuntimeException if no response is received from the server (probably timeout)
   */
  Collection<CommandTagHandle> requestCommandTagHandles(Collection<Long> commandIds);

  /**
   *
   * @param <T> the value type of the command
   * @param commandExecuteRequest the request details for executing this command
   * @return a report about this execution
   * @throws JMSException if a JMS problems occurs or if not connected at the moment
   * @throws RuntimeException if no response is received from the server (probably timeout)
   */
  <T> CommandReport executeCommand(CommandExecuteRequest<T> commandExecuteRequest) throws JMSException;

  /**
   * Requests the DAQ config XML for a given process. Never returns null.
   * @param processName the name of the Process
   * @return the DAQ XML as String
   * @throws JMSException if JMS problem occurs or not connected at the moment
   * @throws RuntimeException if server is unable to answer the request (message contains details)
   */
  String getProcessXml(String processName) throws JMSException;

  /**
   * Requests a list of Names for all the existing processes.
   *
   * @return a list of all process names
   * @throws JMSException if JMS problem occurs or not connected at the moment
   */
  Collection<ProcessNameResponse> getProcessNames() throws JMSException;

  /**
   * Requests a list of all previously applied configuration reports from the
   * server. Note that this method will only return partial information about
   * each report. This is done to reduce the size of the message returned by the
   * server.
   *
   * To get the full report(s) for a particular configuration, use
   * {@link C2monTagManager#getConfigurationReport(Long)}.
   *
   * @return the list of reports
   * @throws JMSException if JMS problem occurs or not connected at the moment
   */
  Collection<ConfigurationReportHeader> getConfigurationReports() throws JMSException;

  /**
   * Requests the full configuration report(s) for a given configuration. Since
   * a configuration may be run more than once, this method returns a collection
   * of all historical reports for the given configuration.
   *
   * @param id the id of the configuration report
   * @return the full configuration report(s) if the configuration was run more
   *         than once
   * @throws JMSException
   */
  Collection<ConfigurationReport> getConfigurationReports(Long id) throws JMSException;
}
