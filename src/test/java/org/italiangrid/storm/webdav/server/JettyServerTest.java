// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
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
    assertThat(jettyServer.getServer().getConnectors().length, is(2));
    ServerConnector c1 = (ServerConnector) jettyServer.getServer().getConnectors()[0];
    assertThat(c1.getPort(), is(8086));
    ServerConnector c2 = (ServerConnector) jettyServer.getServer().getConnectors()[1];
    assertThat(c2.getPort(), is(9443));
    jettyServer.stop();
  }
}
