/*******************************************************************************
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
 ******************************************************************************/
package cern.c2mon.daq.config;

import cern.c2mon.daq.common.conf.core.CommonConfiguration;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.common.messaging.impl.ActiveRequestSender;
import cern.c2mon.daq.common.messaging.impl.TestModeRequestSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;

/**
 * This configuration class is responsible for instantiating the various {@link ProcessRequestSender} beans used within the DAQ core. The {@link
 * ProcessRequestSender}s are responsible for requesting the DAQ configuration from the server.
 *
 * There are three possible modes (specified by the c2mon.daq.mode, which is translated to a Spring {@link Profile}). The three modes are:
 *
 * "single" : a single {@link ProcessRequestSender} requesting from a primary JMS broker
 * "double" : two {@link ProcessRequestSender}s, one requesting from a primary JMS broker and one talking to a secondary broker (e.g. a test server). The
 *            secondary connection is only used to notify the secondary server of disconnection events.
 * "test"   : a single {@link ProcessRequestSender} which requests from a primary JMS broker but does not send disconnection events.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@ImportResource({
    "classpath:daq-activemq.xml",
    "classpath:daq-activemq-second.xml"
})
public class ProcessRequestSenderConfig {

  @Autowired
  Environment environment;

  @Autowired
  CommonConfiguration commonConfiguration;

  @Autowired
  @Qualifier("processRequestJmsTemplate")
  JmsTemplate processRequestJmsTemplate;

  @Autowired
  @Qualifier("secondProcessRequestJmsTemplate")
  JmsTemplate secondProcessRequestJmsTemplate;

  @Bean
  @Profile({ "single", "double", "test" })
  public ProcessRequestSender primaryRequestSender() {
    return new ActiveRequestSender(commonConfiguration, processRequestJmsTemplate);
  }

  @Bean
  @Profile("double")
  public ProcessRequestSender secondaryRequestSender() {
    return new ActiveRequestSender(commonConfiguration, secondProcessRequestJmsTemplate);
  }

  @Bean(name = "primaryRequestSender")
  @Profile("test")
  public ProcessRequestSender testRequestSender() {
    return new TestModeRequestSender(primaryRequestSender());
  }

}
