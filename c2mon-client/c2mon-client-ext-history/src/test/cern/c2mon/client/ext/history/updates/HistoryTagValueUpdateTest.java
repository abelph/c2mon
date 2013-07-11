/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.ext.history.updates;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.HistorySupervisionEvent;
import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.Timespan;
import cern.c2mon.client.ext.history.common.event.HistoryPlayerAdapter;
import cern.c2mon.client.ext.history.dbaccess.HistoryProviderSimpleImpl;
import cern.c2mon.client.ext.history.playback.schedule.HistoryScheduler;
import cern.c2mon.client.ext.history.testUtil.UncaughtExceptionSetup;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;

public class HistoryTagValueUpdateTest {

  /**
   * Tests HistoryTagValueUpdate xml serialization.
   * @throws Exception
   */
  @Test
  public void testTagQualityIsIncludedInXml() throws Exception {
    
    java.util.Date date = new java.util.Date();
    Timestamp now = new Timestamp(date.getTime());
    
    DataTagQualityImpl q = new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN, "It is down!!");
    
    String value = "Value";

    HistoryTagValueUpdateImpl h = new HistoryTagValueUpdateImpl(100L, 
        q, value, 
        now, now, 
        now, "it looks ok", 
        null, TagMode.MAINTENANCE);
    
    Assert.assertTrue (h.getXml().contains("it looks ok"));
    Assert.assertTrue (h.getXml().contains("Value"));
    Assert.assertTrue (h.getXml().contains("MAINTENANCE"));
    Assert.assertTrue (h.getXml().contains("It is down"));
  }  
}
