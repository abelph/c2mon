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
package cern.c2mon.client.common.tag;

import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.command.AuthorizationDetails;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;

/**
 * This interface represents the read-only interface
 * of the command tag, as it is cached on the C2MON
 * Client API.
 * 
 * @author Matthias Braeger
 */
public interface CommandTag<T> {

  /**
   * Returns the unique identifier of this tag
   * @return the DataTag Identifier
   */
  Long getId();

  /**
   * Returns the command tag name
   * @return the unique command tag name for this tag
   */
  String getName();

  /**
   * Returns the description of this command
   * @return the description
   */
  String getDescription();

  /**
   * Returns the last value set on this command tag.
   * @see #getType
   * @return the tag value
   */
  T getValue();

  /**
   * Returns true if the command tag contains a valid handle from the server.
   * @return validity information for the tag
   */
  boolean isExistingCommand();

  /**
   * Returns the type of the tagValue attribute
   * @see #getValue
   * @see #isExistingCommand()
   * @return the class of the tag value, or <code>null</code> if
   *         the command is not existing. 
   */
  Class< ? > getValueType();

  /**
   * Returns the minimum value for this command.
   * If no minimum value has been defined, the method returns null.
   * @return the minimum value for this command
   */
  Comparable< T > getMinValue();

  /**
   * Returns the maximum value for this command.
   * If no maximum value has been defined, the method returns null.
   * @return the maximum value for this command.
   */
  Comparable< T > getMaxValue();
  
  /**
   * Returns the authorizations details for this command. Please notice
   * that the authorizations details have to be casted into the specific
   * implementation. In case of CERN it will be casted into an
   * {@link RbacAuthorizationDetails} object.
   * @return The authorizations details for this command.
   */
  AuthorizationDetails getAuthorizationDetails();
  
  /**
   * @return the hardware address of the command
   */
  HardwareAddress getHardwareAddress();

  /**
   * @return the ID of the DAQ process to which the command is sent
   *         for execution.
   */
  Long getProcessId();

  /**
   * @return the ID of the DAQ equipment to which the command is sent
   *         for execution.
   */
  Long getEquipmentId();

  /**
   * @return an XML representation of this ClientCommandTag
   */
  String getXml();
}
