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
package cern.c2mon.server.configuration.impl;

import java.lang.reflect.Type;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.shared.client.configuration.ConfigurationException;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.configuration.Configuration;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Handles configuration requests received on JMS from clients.
 *
 * <p>
 * The request is processed and a configuration report is returned to the sender
 * (in the form of an XML message on a reply topic specified by the client).
 *
 * @author Mark Brightwell
 * @author Justin Lewis Salmon
 *
 */
@Service("configurationRequestHandler")
public class ConfigurationRequestHandler implements SessionAwareMessageListener<Message> {

  /**
   * Private class logger.
   */
  private static final Logger log = Logger.getLogger(ConfigurationRequestHandler.class);

  /**
   * Reference to the configuration loader.
   */
  @Autowired
  private ConfigurationLoader configurationLoader;

  @Override
  public void onMessage(Message message, Session session) throws JMSException {
    Configuration configuration = new GsonBuilder().registerTypeAdapter(HardwareAddress.class, new InterfaceAdapter<HardwareAddress>()).create()
        .fromJson(TextMessage.class.cast(message).getText(), Configuration.class);
    log.info(String.format("Configuration request received"));

    ConfigurationReport configurationReport;
    try {
      configurationReport = configurationLoader.applyConfiguration(configuration);
    } catch (ConfigurationException ex) {
      configurationReport = ex.getConfigurationReport();
    }

    // Extract reply topic
    Destination replyDestination = null;
    try {
      replyDestination = message.getJMSReplyTo();
    } catch (JMSException jmse) {
      log.error("onMessage() : Cannot extract ReplyTo from message.", jmse);
      throw jmse;
    }

    if (replyDestination != null) {
      MessageProducer messageProducer = null;
      try {
        messageProducer = session.createProducer(replyDestination);
        TextMessage replyMessage = session.createTextMessage();
        replyMessage.setText(configurationReport.toXML());
        if (log.isDebugEnabled()) {
          log.debug("Sending reconfiguration report to client.");
        }
        messageProducer.send(replyMessage);
      } finally {
        if (messageProducer != null) {
            messageProducer.close();
        }
      }
    } else {
      log.error("onMessage(): JMSReplyTo destination is null - cannot send reply.");
      throw new MessageConversionException("JMS reply queue could not be extracted (returned null).");
    }
  }

  final class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    @Override
    public JsonElement serialize(T object, Type interfaceType, JsonSerializationContext context) {
      final JsonObject wrapper = new JsonObject();
      wrapper.addProperty("class", object.getClass().getName());
      wrapper.add("data", context.serialize(object));
      return wrapper;
    }

    @Override
    public T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {
      final JsonObject wrapper = (JsonObject) elem;
      final JsonElement typeName = get(wrapper, "class");
      final JsonElement data = get(wrapper, "data");
      final Type actualType = typeForName(typeName);
      return context.deserialize(data, actualType);
    }

    private Type typeForName(final JsonElement typeElem) {
      try {
        return Class.forName(typeElem.getAsString());
      } catch (ClassNotFoundException e) {
        throw new JsonParseException(e);
      }
    }

    private JsonElement get(final JsonObject wrapper, String memberName) {
      final JsonElement elem = wrapper.get(memberName);
      if (elem == null)
        throw new JsonParseException("no '" + memberName + "' member found in what was expected to be an interface wrapper");
      return elem;
    }
  }
}
