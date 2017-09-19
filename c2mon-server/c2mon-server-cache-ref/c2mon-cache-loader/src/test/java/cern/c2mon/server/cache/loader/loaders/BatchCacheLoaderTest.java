package cern.c2mon.server.cache.loader.loaders;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loader.AlarmLoaderDAO;
import cern.c2mon.server.cache.loader.common.BatchCacheLoader;
import cern.c2mon.server.cache.loader.config.CacheLoaderModuleRef;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.config.CommonModule;

import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CommonModule.class,
        CacheLoaderModuleRef.class,
        CacheDbAccessModule.class
}, loader = AnnotationConfigContextLoader.class)
public class BatchCacheLoaderTest {

  private C2monCache<Long, Alarm> alarmCacheRef;

  @Autowired
  private AlarmLoaderDAO alarmLoaderDAO;

  private BatchCacheLoader<Long, Alarm> batchCacheLoader;

  @Test
  public void preloadCacheFromDb() {
    batchCacheLoader = new BatchCacheLoader<>(alarmCacheRef, alarmLoaderDAO);
    assertNotNull("Cache loader should not be null", batchCacheLoader);
  }
}