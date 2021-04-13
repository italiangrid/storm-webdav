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
package org.italiangrid.storm.webdav.test.tpc.http;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.BufferProperties;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.http.HttpTransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.PathType;

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

  @Mock
  PathResolver resolver;

  @Mock
  ExtendedAttributesHelper eah;

  @Mock
  CloseableHttpClient httpClient;

  @Mock
  GetTransferRequest req;

  @Mock
  ScheduledExecutorService es;

  @SuppressWarnings("rawtypes")
  @Mock
  ScheduledFuture sf;

  HttpTransferClient client;

  @Captor
  ArgumentCaptor<HttpGet> getRequest;

  public static final String MOCKFS_WORKDIR = "/mockfs";

  public FileSystem initMockFs() {
    Configuration fsConfig = Configuration.builder(PathType.unix())
      .setRoots("/")
      .setWorkingDirectory(MOCKFS_WORKDIR)
      .setAttributeViews("basic", "owner", "posix", "unix", "user")
      .build();

    return Jimfs.newFileSystem(fsConfig);
  }

  @Before
  public void setup() throws IOException {
    ThirdPartyCopyProperties props = new ThirdPartyCopyProperties();
    ServiceConfigurationProperties config = new ServiceConfigurationProperties();
    BufferProperties buffer = new BufferProperties();
    config.setBuffer(buffer);

    client =
        new HttpTransferClient(Clock.systemDefaultZone(), httpClient, resolver, eah, es, props,
            config);
  }

}
