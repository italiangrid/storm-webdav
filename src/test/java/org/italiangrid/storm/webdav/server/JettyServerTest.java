// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jetty.JettyWebServer;
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@WithAnonymousUser
class JettyServerTest {

  @Autowired private JettyServletWebServerFactory factory;

  @Test
  void startJettyServer() throws Exception {
    JettyWebServer jettyServer = (JettyWebServer) factory.getWebServer();
    jettyServer.start();
    assertEquals(2, jettyServer.getServer().getConnectors().length);
    ServerConnector c1 = (ServerConnector) jettyServer.getServer().getConnectors()[0];
    assertEquals(8086, c1.getPort());
    ServerConnector c2 = (ServerConnector) jettyServer.getServer().getConnectors()[1];
    assertEquals(9443, c2.getPort());
    jettyServer.stop();
  }
}
