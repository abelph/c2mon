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
package cern.c2mon.server.cache.loader.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.loader.ConfigurableDAO;
import cern.c2mon.server.cache.loader.DataTagLoaderDAO;
import cern.c2mon.server.cache.loader.common.AbstractBatchLoaderDAO;
import cern.c2mon.server.common.datatag.DataTag;

/**
 * DataTag loader DAO implementation.
 *
 * @author Mark Brightwell
 */
//TODO: refer a name
@Service("dataTagLoaderDAORef")
public class DataTagLoaderDAOImpl extends AbstractBatchLoaderDAO<DataTag> implements DataTagLoaderDAO, ConfigurableDAO<DataTag> {

  private DataTagMapper dataTagMapper;

  @Autowired
  public DataTagLoaderDAOImpl(final DataTagMapper dataTagMapper) {
    super(dataTagMapper);
    this.dataTagMapper = dataTagMapper;
  }

  @Override
  public void deleteItem(Long id) {
    dataTagMapper.deleteDataTag(id);
  }

  @Override
  public void insert(DataTag dataTag) {
    dataTagMapper.insertDataTag(dataTag);
  }

  @Override
  public void updateConfig(DataTag dataTag) {
    dataTagMapper.updateConfig(dataTag);
  }

  @Override
  protected DataTag doPostDbLoading(final DataTag item) {
    return item;
  }
}