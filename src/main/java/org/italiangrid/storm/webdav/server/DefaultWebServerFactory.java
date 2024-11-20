/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.italiangrid.storm.webdav.server;

import java.util.concurrent.ArrayBlockingQueue;

import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.metrics.jetty12.InstrumentedQueuedThreadPool;

public class DefaultWebServerFactory
    implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {

  final ServiceConfiguration configuration;
  final ServerProperties serverProperties;
  final MetricRegistry metricRegistry;
  final JettyServerCustomizer serverCustomizer;

  public DefaultWebServerFactory(ServiceConfiguration configuration,
      ServerProperties serverProperties, JettyServerCustomizer serverCustomizer,
      MetricRegistry registry) {

    this.configuration = configuration;
    this.serverProperties = serverProperties;
    this.serverCustomizer = serverCustomizer;
    this.metricRegistry = registry;
  }

  private InstrumentedQueuedThreadPool getInstrumentedThreadPool() {
    InstrumentedQueuedThreadPool tPool = new InstrumentedQueuedThreadPool(metricRegistry,
        configuration.getMaxConnections(), configuration.getMinConnections(),
        configuration.getThreadPoolMaxIdleTimeInMsec(),
        new ArrayBlockingQueue<>(configuration.getMaxQueueSize()), new ThreadGroup("storm.http"));
    tPool.setName("thread-pool");
    return tPool;
  }

  @Override
  public void customize(JettyServletWebServerFactory factory) {

    factory.setThreadPool(getInstrumentedThreadPool());
    factory.addServerCustomizers(serverCustomizer);
  }

}
