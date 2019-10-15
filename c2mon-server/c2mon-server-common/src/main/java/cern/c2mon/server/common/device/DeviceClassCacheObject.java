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
package cern.c2mon.server.common.device;

import cern.c2mon.server.common.AbstractCacheableImpl;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class implements the <code>DeviceClass</code> interface and resides in
 * the server DeviceClass cache.
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class DeviceClassCacheObject extends AbstractCacheableImpl implements DeviceClass {

  /**
   * Serial version UID, since cloneable
   */
  private static final long serialVersionUID = 5797114724330150853L;

  /**
   * The name of the device class.
   */
  private String name;

  /**
   * The textual description of the device class.
   */
  private String description;

  /**
   * The list of properties that belong to this device class.
   */
  private List<Property> properties = new ArrayList<>();

  /**
   * The list of commands that belong o this device class.
   */
  private List<Command> commands = new ArrayList<>();

  /**
   * List of IDs of devices that are instances of this class.
   */
  private List<Long> deviceIds = new ArrayList<>();

  /**
   * Default constructor.
   *
   * @param id          the unique ID of the device class
   * @param name        the name of the device class
   * @param description the textual description of the device class
   */
  public DeviceClassCacheObject(final Long id, final String name, final String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  /**
   * Constructor used when creating a cache object during configuration.
   *
   * @param id the unique ID of the device class
   */
  public DeviceClassCacheObject(final Long id) {
    this(id, null, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public DeviceClassCacheObject clone() throws CloneNotSupportedException {
    DeviceClassCacheObject clone = (DeviceClassCacheObject) super.clone();

    clone.properties = (List<Property>) ((ArrayList<Property>) properties).clone();
    clone.commands = (List<Command>) ((ArrayList<Command>) commands).clone();
    clone.deviceIds = (List<Long>) ((ArrayList<Long>) deviceIds).clone();

    return clone;
  }

  @Override
  public List<Long> getPropertyIds() {
    return properties.stream().map(Property::getId).filter(Objects::nonNull).collect(Collectors.toList());
  }

  @Override
  public List<Long> getCommandIds() {
    return commands.stream().map(Command::getId).filter(Objects::nonNull).collect(Collectors.toList());
  }

  @Override
  public List<String> getPropertyNames() {
    return properties.stream().map(Property::getName).filter(Objects::nonNull).collect(Collectors.toList());
  }

  @Override
  public List<String> getCommandNames() {
    return commands.stream().map(Command::getName).filter(Objects::nonNull).collect(Collectors.toList());
  }

  @Override
  public Long getPropertyId(String name) {
    return properties.stream()
      .filter(prop -> prop.getName().equals(name)).findFirst()
      // TODO This code is a very suboptimal use of Optional. It has been chosen only to maintain
      //  backwards compatibility with previous callers and should be dropped eventually
      .map(Property::getId).orElse(null);
  }

  @Override
  public Long getCommandId(String name) {
    // TODO Same as above about suboptimal Optional
    return commands.stream().filter(command -> command.getName().equals(name)).findFirst().map(Command::getId).orElse(null);
  }

  @Override
  public List<String> getFieldNames(String propertyName) {
    return mapPropertyFields(propertyName, Property::getName);
  }

  @Override
  public List<Long> getFieldIds(String propertyName) {
    return mapPropertyFields(propertyName, Property::getId);
  }


  private <T> List<T> mapPropertyFields(String propertyName, Function<Property, T> mapper) {
    return properties.stream().filter(prop -> prop.getName().equals(propertyName))
      .findFirst().map(Property::getFields).orElse(new ArrayList<>())
      .stream().map(mapper).collect(Collectors.toList());
  }
}
