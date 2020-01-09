package cern.c2mon.cache.impl;

import lombok.experimental.Delegate;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.inject.Named;
import javax.inject.Singleton;

import static cern.c2mon.cache.impl.C2monCacheProperties.METRICS_LOG_FREQUENCY;

/**
 * Made as a copy of {@link org.apache.ignite.IgniteSpringBean}, to work around the limitation it imposes:
 * Having to wait until the {@link org.springframework.context.event.ContextRefreshedEvent} for the bean to be ready
 *
 * This class should not be initialized twice, unless it's been destroyed properly!
 * See {@code IgniteTest} for some of the expected behaviors of the {@code Ignite} class
 * For example, it can't be started twice!
 *
 * @see <a href=https://ignite.apache.org/releases/latest/javadoc/org/apache/ignite/IgniteSpringBean.html>IgniteSpringBean doc</a>
 *
 * @author Alexandros Papageorgiou Koufidis
 */
@Named("C2monIgnite")
@Singleton
public class IgniteC2monBean implements Ignite, DisposableBean {

  // Delegate is lombok experimental - https://projectlombok.org/features/experimental/Delegate
  // Skips us a lot of ugly code though
  @Delegate(types = Ignite.class)
  private Ignite igniteInstance;

  public IgniteC2monBean() {
    // Not in a try because we want to fail-fast if there's a problem here
    igniteInstance = Ignition.start(defaultConfiguration());
  }

  @Override
  public void destroy() throws Exception {
    try {
      igniteInstance.close();
    } catch (IgniteException exception) {
      // This can also just log a warning and fail quietly
      throw new Exception(exception);
    }
  }

  private IgniteConfiguration defaultConfiguration() {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ignite-config.xml");

    IgniteConfiguration config = (IgniteConfiguration) context.getBean("base-ignite.cfg");

    config.setGridLogger(new Slf4jLogger());

    config.setMetricsLogFrequency(METRICS_LOG_FREQUENCY);

    return config;
  }
}
