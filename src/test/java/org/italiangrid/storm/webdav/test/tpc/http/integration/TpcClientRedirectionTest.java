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
package org.italiangrid.storm.webdav.test.tpc.http.integration;

import static org.mockserver.configuration.Configuration.configuration;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.NottableString.not;
import static org.mockserver.verify.VerificationTimes.exactly;

import java.net.URI;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.italiangrid.storm.webdav.WebdavService;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.test.tpc.http.integration.TpcClientRedirectionTest.TestConfig;
import org.italiangrid.storm.webdav.tpc.http.HttpTransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.impl.GetTransferRequestImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpResponse;
import org.mockserver.socket.PortFactory;
import org.mockserver.socket.tls.KeyStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {WebdavService.class, TestConfig.class},
    properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("dev")
public class TpcClientRedirectionTest {

  public static final Logger LOG = LoggerFactory.getLogger(TpcIntegrationTest.class);

  private static int httpPort;
  private static int httpsPort;

  private static ClientAndServer mockServer;

  @Autowired
  HttpTransferClient client;

  @Configuration
  public static class TestConfig {

    @Bean("tpcConnectionManager")
    @Primary
    public HttpClientConnectionManager tpcClientConnectionManager(ThirdPartyCopyProperties props,
        ServiceConfiguration conf) {
      return PoolingHttpClientConnectionManagerBuilder.create()
        .setMaxConnTotal(props.getMaxConnections())
        .build();
    }
  }

  @BeforeAll
  static void startMockServer() {
    // Ensure all connection using HTTPS will use the SSL context defined by
    // MockServer to allow dynamically generated certificates to be accepted
    HttpsURLConnection.setDefaultSSLSocketFactory(
        new KeyStoreFactory(configuration(), new MockServerLogger()).sslContext()
          .getSocketFactory());
    httpPort = PortFactory.findFreePort();
    httpsPort = httpPort + 1;
    mockServer = startClientAndServer(httpPort, httpsPort);
  }

  @AfterAll
  static void stopMockServer() {
    mockServer.stop();
  }

  @BeforeEach
  void before() {
    mockServer.reset();

  }

  private String mockHttpsUrl(String path) {
    return String.format("https://localhost:%d%s", httpsPort, path);
  }

  private String mockHttpUrl(String path) {
    return String.format("http://localhost:%d%s", httpPort, path);
  }

  @Test
  void handleCrossProtocolRedirectionCorrectly() {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer this-is-a-fake-token");


    GetTransferRequest getRequest = new GetTransferRequestImpl(UUID.randomUUID().toString(),
        "/test/example", URI.create(mockHttpsUrl("/test/example")), headers, null, false, false);

    mockServer
      .when(request().withMethod("GET").withPath("/test/example").withSecure(true),
          Times.exactly(1))
      .respond(HttpResponse.response()
        .withStatusCode(307)
        .withHeader("Location", mockHttpUrl("/redirected/test/example")));

    mockServer
      .when(request().withMethod("GET").withPath("/redirected/test/example").withSecure(false),
          Times.exactly(1))
      .respond(HttpResponse.response().withStatusCode(200).withBody("example"));

    client.handle(getRequest, (r, s) -> {
      // do nothing here
    });

    mockServer.verify(
        request().withMethod("GET").withPath("/test/example").withHeaders(header("Authorization")),
        exactly(1));

    mockServer.verify(request().withMethod("GET")
      .withPath("/redirected/test/example")
      .withHeaders(header(not("Authorization"))), exactly(1));
  }
}
