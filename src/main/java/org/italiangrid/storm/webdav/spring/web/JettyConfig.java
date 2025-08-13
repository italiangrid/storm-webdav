// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.spring.web;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import org.italiangrid.storm.webdav.config.ConfigurationLogger;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.server.DefaultJettyServerCustomizer;
import org.italiangrid.storm.webdav.server.DefaultJettyServletWebServerFactory;
import org.italiangrid.storm.webdav.server.DefaultWebServerFactory;
import org.springframework.boot.jetty.JettyServerCustomizer;
import org.springframework.boot.jetty.autoconfigure.JettyServerProperties;
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JettyConfig {

  @Bean
  JettyServerCustomizer defaultJettyServerCustomizer(
      ServiceConfigurationProperties serviceConfig,
      ServiceConfiguration configuration,
      StorageAreaConfiguration saConf,
      JettyServerProperties serverProperties,
      ConfigurationLogger confLogger,
      X509CertChainValidatorExt certChainValidator) {

    return new DefaultJettyServerCustomizer(
        serviceConfig, configuration, saConf, serverProperties, confLogger, certChainValidator);
  }

  @Bean
  WebServerFactoryCustomizer<JettyServletWebServerFactory> defaultWebServerFactory(
      ServiceConfiguration configuration, JettyServerCustomizer serverCustomizer) {

    return new DefaultWebServerFactory(configuration, serverCustomizer);
  }

  @Bean
  JettyServletWebServerFactory defaultJettyServletWebServerFactory() {
    return new DefaultJettyServletWebServerFactory();
  }
}
