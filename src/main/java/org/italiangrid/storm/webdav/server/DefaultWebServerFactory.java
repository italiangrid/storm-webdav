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

import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.server.util.ThreadPoolBuilder;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

import com.codahale.metrics.MetricRegistry;

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

  @Override
  public void customize(JettyServletWebServerFactory factory) {

    factory.setThreadPool(ThreadPoolBuilder.instance()
      .withMaxRequestQueueSize(configuration.getMaxQueueSize())
      .withMaxThreads(serverProperties.getJetty().getThreads().getMax())
      .withMinThreads(serverProperties.getJetty().getThreads().getMin())
      .registry(metricRegistry)
      .withPrefix("storm.http")
      .withName("thread-pool")
      .build());

    factory.addServerCustomizers(serverCustomizer);
  }

}
