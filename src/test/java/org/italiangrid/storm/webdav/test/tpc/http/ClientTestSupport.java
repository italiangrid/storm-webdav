// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc.http;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.BufferProperties;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.http.HttpComponentsMetrics;
import org.italiangrid.storm.webdav.tpc.http.HttpTransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

public class ClientTestSupport {

  public static final String SA_ROOT = "/test";
  public static final String LOCAL_PATH = "/test/file";
  public static final String HTTP_URI = "http://storm.example/test/file";
  public static final URI HTTP_URI_URI = URI.create(HTTP_URI);

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String AUTHORIZATION_HEADER_VALUE = "Bearer 12345";

  public static final Multimap<String, String> HEADER_MAP =
      new ImmutableMultimap.Builder<String, String>()
          .put(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE)
          .build();

  @Mock PathResolver resolver;

  @Mock ExtendedAttributesHelper eah;

  @Mock CloseableHttpClient httpClient;

  @Mock GetTransferRequest req;

  @Mock ScheduledExecutorService es;

  @SuppressWarnings("rawtypes")
  @Mock
  ScheduledFuture sf;

  @Autowired ObservationRegistry observationRegistry;

  @Mock MeterRegistry meterRegistry;

  HttpTransferClient client;

  @Captor ArgumentCaptor<BasicClassicHttpRequest> getRequest;

  @BeforeEach
  void setup() throws IOException {
    ThirdPartyCopyProperties props = new ThirdPartyCopyProperties();
    ServiceConfigurationProperties config = new ServiceConfigurationProperties();
    BufferProperties buffer = new BufferProperties();
    config.setBuffer(buffer);
    HttpComponentsMetrics httpComponentsMetrics = new HttpComponentsMetrics(meterRegistry);

    client =
        new HttpTransferClient(
            Clock.systemDefaultZone(),
            httpClient,
            resolver,
            eah,
            es,
            props,
            config,
            observationRegistry,
            httpComponentsMetrics);
  }
}
