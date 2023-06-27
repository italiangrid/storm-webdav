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
package org.italiangrid.storm.webdav.spring.web;

import org.italiangrid.storm.webdav.config.ConfigurationLogger;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.server.DefaultJettyServerCustomizer;
import org.italiangrid.storm.webdav.server.DefaultJettyServletWebServerFactory;
import org.italiangrid.storm.webdav.server.DefaultWebServerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;

@Configuration
public class JettyConfig {

  @Bean
  JettyServerCustomizer defaultJettyServerCustomizer(ServiceConfigurationProperties serviceConfig,
      ServiceConfiguration configuration, StorageAreaConfiguration saConf,
      ServerProperties serverProperties, MetricRegistry registry, ConfigurationLogger confLogger,
      X509CertChainValidatorExt certChainValidator) {

    return new DefaultJettyServerCustomizer(serviceConfig, configuration, saConf, serverProperties,
        registry, confLogger, certChainValidator);

  }

  @Bean
  WebServerFactoryCustomizer<JettyServletWebServerFactory> defaultWebServerFactory(
      ServiceConfiguration configuration, ServerProperties serverProperties,
      JettyServerCustomizer serverCustomizer, MetricRegistry registry) {

    return new DefaultWebServerFactory(configuration, serverProperties, serverCustomizer, registry);
  }

  @Bean
  JettyServletWebServerFactory defaultJettyServletWebServerFactory() {
    return new DefaultJettyServletWebServerFactory();
  }
}
