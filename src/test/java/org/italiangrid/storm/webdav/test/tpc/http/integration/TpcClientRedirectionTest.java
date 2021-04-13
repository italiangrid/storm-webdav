/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.NottableString.not;
import static org.mockserver.verify.VerificationTimes.exactly;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.italiangrid.storm.webdav.WebdavService;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.test.tpc.http.integration.TpcClientRedirectionTest.TestConfig;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.storm.webdav.tpc.http.HttpTransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.impl.GetTransferRequestImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
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
        ServiceConfiguration conf) throws KeyStoreException, CertificateException, IOException,
        NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {

      SSLContext ctx = KeyStoreFactory.keyStoreFactory().sslContext();
      ConnectionSocketFactory sf = PlainConnectionSocketFactory.getSocketFactory();
      LayeredConnectionSocketFactory tlsSf = new SSLConnectionSocketFactory(ctx);

      Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
        .register(TransferConstants.HTTP, sf)
        .register(TransferConstants.HTTPS, tlsSf)
        .register(TransferConstants.DAV, sf)
        .register(TransferConstants.DAVS, tlsSf)
        .build();

      PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);
      cm.setMaxTotal(props.getMaxConnections());
      return cm;
    }
  }

  @BeforeClass
  public static void startMockServer() {
    // port = findAvailableTcpPort(15000);

    httpPort = PortFactory.findFreePort();
    httpsPort = httpPort + 1;
    mockServer = startClientAndServer(httpPort, httpsPort);


  }

  @AfterClass
  public static void stopMockServer() {
    mockServer.stop();
  }

  @Before
  public void before() {
    mockServer.reset();

  }

  private String mockHttpsUrl(String path) {
    return String.format("https://localhost:%d%s", httpsPort, path);
  }

  private String mockHttpUrl(String path) {
    return String.format("http://localhost:%d%s", httpPort, path);
  }

  @Test
  public void handleCrossProtocolRedirectionCorrectly() {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer this-is-a-fake-token");


    GetTransferRequest getRequest =
        new GetTransferRequestImpl(UUID.randomUUID().toString(),
            "/test/example", URI.create(mockHttpsUrl("/test/example")), headers, false, false);

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
