// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server;

import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jetty.JettyServerCustomizer;
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

public class DefaultWebServerFactory
    implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {

  @Value("${spring.threads.virtual.enabled:false}")
  private boolean virtualThreadsEnabled;

  final ServiceConfiguration configuration;
  final JettyServerCustomizer serverCustomizer;

  public DefaultWebServerFactory(
      ServiceConfiguration configuration, JettyServerCustomizer serverCustomizer) {

    this.configuration = configuration;
    this.serverCustomizer = serverCustomizer;
  }

  @Override
  public void customize(JettyServletWebServerFactory factory) {
    factory.addServerCustomizers(serverCustomizer);
  }
}
