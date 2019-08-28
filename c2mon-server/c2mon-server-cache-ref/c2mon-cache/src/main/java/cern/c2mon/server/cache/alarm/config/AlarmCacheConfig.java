package cern.c2mon.server.cache.alarm.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.AbstractBatchCacheConfig;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.AlarmLoaderDAO;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.server.common.alarm.Alarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class AlarmCacheConfig extends AbstractBatchCacheConfig<Alarm> {

  @Autowired
  public AlarmCacheConfig(ThreadPoolTaskExecutor cacheLoaderTaskExecutor, CacheLoaderProperties properties, AbstractCacheFactory cachingFactory, AlarmLoaderDAO alarmLoaderDAORef) {
    super(cachingFactory, CacheName.ALARM, Alarm.class, cacheLoaderTaskExecutor, properties, alarmLoaderDAORef);
  }

  @Bean(name = CacheName.Names.ALARM)
  @Override
  public C2monCache<Alarm> createCache() {
    return super.createCache();
  }
}