// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc.http.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.tpc.http.HttpTransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;
import org.italiangrid.storm.webdav.tpc.transfer.impl.GetTransferRequestImpl;
import org.italiangrid.storm.webdav.tpc.transfer.impl.PutTransferRequestImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class TpcIntegrationTest {

  private static String authorizationHeaderValue = "Bearer this-is-a-fake-token";

  @Autowired HttpTransferClient client;

  @RegisterExtension
  private static final WireMockExtension wiremock =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  private String mockUrl(String path) {
    return String.format("http://localhost:%d%s", wiremock.getRuntimeInfo().getHttpPort(), path);
  }

  @Test
  void testPutRedirectHandled() {
    Multimap<String, String> emptyHeaders = ArrayListMultimap.create();

    PutTransferRequest putRequest =
        new PutTransferRequestImpl(
            UUID.randomUUID().toString(),
            "/test/example",
            URI.create(mockUrl("/test/example")),
            emptyHeaders,
            new SciTag(1, 2, true),
            Optional.empty(),
            true);

    wiremock.stubFor(
        put("/test/example").willReturn(temporaryRedirect("/redirected/test/example")));

    wiremock.stubFor(put("/redirected/test/example").willReturn(unauthorized()));

    client.handle(
        putRequest,
        (r, s) -> {
          assertEquals(TransferStatus.Status.ERROR, s.getStatus());
          assertTrue(s.getErrorMessage().isPresent());
          assertTrue(
              s.getErrorMessage().get().contains("status code: 401, reason phrase: Unauthorized"));
        });

    wiremock.verify(1, putRequestedFor(urlEqualTo("/test/example")));
    wiremock.verify(1, putRequestedFor(urlEqualTo("/redirected/test/example")));
  }

  @Test
  void testAuthorizationHeaderIsDroppedOnRedirectForPut() {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);

    PutTransferRequest putRequest =
        new PutTransferRequestImpl(
            UUID.randomUUID().toString(),
            "/test/example",
            URI.create(mockUrl("/test/example")),
            headers,
            null,
            Optional.empty(),
            true);

    wiremock.stubFor(
        put("/test/example").willReturn(temporaryRedirect("/redirected/test/example")));

    wiremock.stubFor(put("/redirected/test/example").willReturn(created()));

    client.handle(
        putRequest,
        (r, s) -> {
          // do nothing here
        });

    wiremock.verify(
        1,
        putRequestedFor(urlEqualTo("/test/example"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(authorizationHeaderValue)));

    wiremock.verify(
        1,
        putRequestedFor(urlEqualTo("/redirected/test/example"))
            .withHeader(HttpHeaders.AUTHORIZATION, absent()));
  }

  @Test
  void testAuthorizationHeaderIsDroppedOnRedirectForGet() {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);

    GetTransferRequest getRequest =
        new GetTransferRequestImpl(
            UUID.randomUUID().toString(),
            "/test/example",
            URI.create(mockUrl("/test/example")),
            headers,
            null,
            Optional.empty(),
            false);

    wiremock.stubFor(
        get("/test/example").willReturn(permanentRedirect("/redirected/test/example")));

    wiremock.stubFor(get("/redirected/test/example").willReturn(ok().withBody("example")));

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
