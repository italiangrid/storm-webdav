// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.metrics.jetty12.InstrumentedQueuedThreadPool;
import java.util.concurrent.ArrayBlockingQueue;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

public class DefaultWebServerFactory
    implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {

  final ServiceConfiguration configuration;
  final ServerProperties serverProperties;
  final MetricRegistry metricRegistry;
  final JettyServerCustomizer serverCustomizer;

  public DefaultWebServerFactory(
      ServiceConfiguration configuration,
      ServerProperties serverProperties,
      JettyServerCustomizer serverCustomizer,
      MetricRegistry registry) {

    this.configuration = configuration;
    this.serverProperties = serverProperties;
    this.serverCustomizer = serverCustomizer;
    this.metricRegistry = registry;
  }

  private InstrumentedQueuedThreadPool getInstrumentedThreadPool() {
    InstrumentedQueuedThreadPool tPool =
        new InstrumentedQueuedThreadPool(
            metricRegistry,
            configuration.getMaxConnections(),
            configuration.getMinConnections(),
            configuration.getThreadPoolMaxIdleTimeInMsec(),
            new ArrayBlockingQueue<>(configuration.getMaxQueueSize()),
            new ThreadGroup("storm.http"));
    tPool.setName("thread-pool");
    return tPool;
  }

  @Override
  public void customize(JettyServletWebServerFactory factory) {

    factory.setThreadPool(getInstrumentedThreadPool());
    factory.addServerCustomizers(serverCustomizer);
  }
}
