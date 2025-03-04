// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static org.mockito.Mockito.lenient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Enumeration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.hc.core5.http.HeaderElements;
import org.apache.hc.core5.http.HttpHeaders;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.italiangrid.storm.webdav.tpc.StaticHostListLocalURLService;
import org.italiangrid.storm.webdav.tpc.TransferFilter;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public class TransferFilterTestSupport {

  public static final Instant NOW = Instant.parse("2021-01-01T00:00:00.00Z");

  Clock clock = Clock.fixed(NOW, ZoneId.systemDefault());

  public static final String SERVLET_PATH = "/test";
  public static final String LOCAL_PATH = "/some/file";
  public static final String LOCAL_PATH_PARENT = "/some";

  public static final String FULL_LOCAL_PATH = SERVLET_PATH + LOCAL_PATH;
  public static final String FULL_LOCAL_PATH_PARENT = SERVLET_PATH + LOCAL_PATH_PARENT;

  public static final String HTTP_URL = "http://storm.example/test/some/file";
  public static final String HTTPS_URL = "https://storm.example/test/some/file";
  public static final String DAV_URL = "dav://storm.example/test/some/file";
  public static final String DAVS_URL = "davs://storm.example/test/some/file";

  public static final String TRANSFER_HEADER = "TransferHeader";
  public static final String TRANSFER_HEADER_AUTHORIZATION_KEY = "TransferHeaderAuthorization";
  public static final String TRANSFER_HEADER_AUTHORIZATION_VALUE = "Bearer 123456";

  public static final String TRANSFER_HEADER_WHATEVER_KEY = "TransferHeaderWhatever";
  public static final String TRANSFER_HEADER_WHATEVER_VALUE = "papisilviobelluscona";

  public static final String SCITAG_HEADER = "SciTag";
  public static final String SCITAG_HEADER_VALUE = "65";
  public static final String TRANSFER_HEADER_SCITAG = "TransferHeaderSciTag";

  public static final URI HTTP_URL_URI = URI.create(HTTP_URL);
  public static final URI HTTPS_URL_URI = URI.create(HTTPS_URL);

  public static final String[] INVALID_URLs =
      {"http:whatever", "httpg://storm.example/test", "gsiftp://whatever/test"};

  public static final String EXPECTED_HEADER = HttpHeaders.EXPECT;
  public static final String EXPECTED_VALUE = HeaderElements.CONTINUE;

  @Mock
  FilterChain chain;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  PrintWriter responseWriter;

  @Mock
  TransferClient client;

  @Mock
  PathResolver resolver;

  @Mock
  Enumeration<String> requestHeaderNames;

  TransferFilter filter;

  @Captor
  ArgumentCaptor<String> error;

  @Captor
  ArgumentCaptor<Integer> httpStatus;

  @Captor
  ArgumentCaptor<GetTransferRequest> getXferRequest;

  @Captor
  ArgumentCaptor<PutTransferRequest> putXferRequest;

  LocalURLService lus = new StaticHostListLocalURLService(Arrays.asList("localhost"));

  protected void setup() throws IOException {
    filter = new TransferFilter(clock, client, resolver, lus, true, 1024L * 1024L);
    lenient().when(request.getHeaderNames()).thenReturn(requestHeaderNames);

  }

}
