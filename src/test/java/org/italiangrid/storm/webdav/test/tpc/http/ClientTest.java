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
package org.italiangrid.storm.webdav.test.tpc.http;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ArrayListMultimap;

@ExtendWith(MockitoExtension.class)
class ClientTest extends ClientTestSupport {

  @TempDir
  public File storage;

  @SuppressWarnings("unchecked")
  @Override
  @BeforeEach
  public void setup() throws IOException {

    super.setup();
    lenient()
      .when(es.scheduleAtFixedRate(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(),
          Mockito.any()))
      .thenReturn(sf);
    lenient().when(req.remoteURI()).thenReturn(HTTP_URI_URI);
    lenient().when(req.path()).thenReturn(LOCAL_PATH);
    lenient().when(req.transferHeaders()).thenReturn(ArrayListMultimap.create());

    Path saRootPath = Paths.get(storage.getAbsolutePath(), SA_ROOT);
    Path localFilePath = Paths.get(storage.getAbsolutePath(), LOCAL_PATH);

    Files.createDirectory(saRootPath);

    lenient().when(resolver.resolvePath(LOCAL_PATH)).thenReturn(localFilePath.toString());


  }

  @Test
  void testClientCorrectlyBuildsHttpRequestNoHeaders() throws IOException, URISyntaxException {

    client.handle(req, (r, s) -> {
    });

    verify(httpClient).execute(getRequest.capture(), ArgumentMatchers.<HttpClientContext>any(),
        ArgumentMatchers.<HttpClientResponseHandler<Boolean>>any());

    BasicClassicHttpRequest httpGetReq = getRequest.getValue();

    assertThat(httpGetReq.getUri(), is(HTTP_URI_URI));
    assertThat(httpGetReq.getHeaders(), arrayWithSize(0));

  }

  @Test
  void testClientCorrectlyBuildsHttpRequestWithHeaders() throws IOException, URISyntaxException {

    when(req.transferHeaders()).thenReturn(HEADER_MAP);

    client.handle(req, (r, s) -> {
    });

    verify(httpClient).execute(getRequest.capture(), ArgumentMatchers.<HttpClientContext>any(),
        ArgumentMatchers.<HttpClientResponseHandler<Boolean>>any());

    BasicClassicHttpRequest httpGetReq = getRequest.getValue();

    assertThat(httpGetReq.getUri(), is(HTTP_URI_URI));
    assertThat(httpGetReq.getHeaders(), arrayWithSize(1));
    assertThat(httpGetReq.getHeaders(AUTHORIZATION_HEADER)[0].getValue(),
        is(AUTHORIZATION_HEADER_VALUE));

  }

}
