// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc.http.integration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.NottableString.not;
import static org.mockserver.verify.VerificationTimes.exactly;
import static org.springframework.test.util.TestSocketUtils.findAvailableTcpPort;

import java.net.URI;
import java.util.UUID;

import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.tpc.http.HttpTransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;
import org.italiangrid.storm.webdav.tpc.transfer.impl.GetTransferRequestImpl;
import org.italiangrid.storm.webdav.tpc.transfer.impl.PutTransferRequestImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class TpcIntegrationTest {

  public static final Logger LOG = LoggerFactory.getLogger(TpcIntegrationTest.class);

  private static int port;

  private static ClientAndServer mockServer;

  @Autowired
  HttpTransferClient client;


  @BeforeAll
  static void startMockServer() {
    port = findAvailableTcpPort();
    mockServer = startClientAndServer(port);
  }

  @AfterAll
  static void stopMockServer() {
    mockServer.stop();
  }

  @BeforeEach
  void before() {
    mockServer.reset();
  }

  private String mockUrl(String path) {
    return String.format("http://localhost:%d%s", port, path);
  }

  @Test
  void testPutRedirectHandled() {
    Multimap<String, String> emptyHeaders = ArrayListMultimap.create();

    PutTransferRequest putRequest = new PutTransferRequestImpl(UUID.randomUUID().toString(),
        "/test/example", URI.create(mockUrl("/test/example")), emptyHeaders, new SciTag(1, 2, true),
        false, true);

    mockServer.when(request().withMethod("PUT").withPath("/test/example"), Times.exactly(1))
      .respond(HttpResponse.response()
        .withStatusCode(307)
        .withHeader("Location", mockUrl("/redirected/test/example")));

    mockServer
      .when(request().withMethod("PUT").withPath("/redirected/test/example"), Times.exactly(1))
      .respond(HttpResponse.response().withStatusCode(401));

    client.handle(putRequest, (r, s) -> {
      assertThat(s.getStatus(), is(TransferStatus.Status.ERROR));
      assertThat(s.getErrorMessage().isPresent(), is(true));
      assertThat(s.getErrorMessage().get(),
          containsString("status code: 401, reason phrase: Unauthorized"));
    });

    mockServer.verify(request().withMethod("PUT").withPath("/test/example"), exactly(1));
    mockServer.verify(request().withMethod("PUT").withPath("/redirected/test/example"), exactly(1));

  }

  @Test
  void testAuthorizationHeaderIsDroppedOnRedirectForPut() {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer this-is-a-fake-token");

    PutTransferRequest putRequest = new PutTransferRequestImpl(UUID.randomUUID().toString(),
        "/test/example", URI.create(mockUrl("/test/example")), headers, null, false, true);

    mockServer.when(request().withMethod("PUT").withPath("/test/example"), Times.exactly(1))
      .respond(HttpResponse.response()
        .withStatusCode(307)
        .withHeader("Location", mockUrl("/redirected/test/example")));

    mockServer
      .when(request().withMethod("PUT").withPath("/redirected/test/example"), Times.exactly(1))
      .respond(HttpResponse.response().withStatusCode(201));

    client.handle(putRequest, (r, s) -> {
      // do nothing here
    });


    mockServer.verify(
        request().withMethod("PUT").withPath("/test/example").withHeaders(header("Authorization")),
        exactly(1));

    mockServer.verify(request().withMethod("PUT")
      .withPath("/redirected/test/example")
      .withHeaders(header(not("Authorization"))), exactly(1));
  }

  @Test
  void testAuthorizationHeaderIsDroppedOnRedirectForGet() {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer this-is-a-fake-token");


    GetTransferRequest getRequest = new GetTransferRequestImpl(UUID.randomUUID().toString(),
        "/test/example", URI.create(mockUrl("/test/example")), headers, null, false, false);


    mockServer.when(request().withMethod("GET").withPath("/test/example"), Times.exactly(1))
      .respond(HttpResponse.response()
        .withStatusCode(302)
        .withHeader("Location", mockUrl("/redirected/test/example")));

    mockServer
      .when(request().withMethod("GET").withPath("/redirected/test/example"), Times.exactly(1))
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
