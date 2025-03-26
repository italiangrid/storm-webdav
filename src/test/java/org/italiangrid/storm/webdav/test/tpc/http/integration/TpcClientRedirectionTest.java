// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc.http.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.http.ssl.TrustSelfSignedStrategy;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.italiangrid.storm.webdav.WebdavService;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.test.tpc.http.integration.TpcClientRedirectionTest.TestConfig;
import org.italiangrid.storm.webdav.tpc.http.HttpTransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.impl.GetTransferRequestImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {WebdavService.class, TestConfig.class},
    properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("dev")
public class TpcClientRedirectionTest {

  public static final Logger LOG = LoggerFactory.getLogger(TpcIntegrationTest.class);

  private static String authorizationHeaderValue = "Bearer this-is-a-fake-token";

  @RegisterExtension
  static WireMockExtension wiremock =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
          .build();

  @Autowired HttpTransferClient client;

  @Configuration
  public static class TestConfig {

    @Bean("tpcConnectionManager")
    @Primary
    public HttpClientConnectionManager tpcClientConnectionManager(
        ThirdPartyCopyProperties props, ServiceConfiguration conf)
        throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
      return PoolingHttpClientConnectionManagerBuilder.create()
          .setMaxConnTotal(props.getMaxConnections())
          .setTlsSocketStrategy(
              new DefaultClientTlsStrategy(
                  SSLContextBuilder.create()
                      .loadTrustMaterial(new TrustSelfSignedStrategy())
                      .build(),
                  NoopHostnameVerifier.INSTANCE))
          .build();
    }
  }

  private String mockHttpsUrl(String path) {
    return String.format("https://localhost:%d%s", wiremock.getRuntimeInfo().getHttpsPort(), path);
  }

  private String mockHttpUrl(String path) {
    return String.format("http://localhost:%d%s", wiremock.getRuntimeInfo().getHttpPort(), path);
  }

  @Test
  void handleCrossProtocolRedirectionCorrectly() {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);

    GetTransferRequest getRequest =
        new GetTransferRequestImpl(
            UUID.randomUUID().toString(),
            "/test/example",
            URI.create(mockHttpsUrl("/test/example")),
            headers,
            null,
            false,
            false);

    wiremock.stubFor(
        get("/test/example")
            .withPort(wiremock.getRuntimeInfo().getHttpsPort())
            .willReturn(temporaryRedirect(mockHttpUrl("/redirected/test/example"))));

    wiremock.stubFor(
        get("/redirected/test/example")
            .withPort(wiremock.getRuntimeInfo().getHttpPort())
            .willReturn(ok().withBody("example")));

    client.handle(
        getRequest,
        (r, s) -> {
          // do nothing here
        });

    wiremock.verify(
        1,
        getRequestedFor(urlEqualTo("/test/example"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(authorizationHeaderValue)));

    wiremock.verify(
        1,
        getRequestedFor(urlEqualTo("/redirected/test/example"))
            .withHeader(HttpHeaders.AUTHORIZATION, absent()));
  }
}
