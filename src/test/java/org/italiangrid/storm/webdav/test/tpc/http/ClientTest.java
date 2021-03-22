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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ArrayListMultimap;

@RunWith(MockitoJUnitRunner.class)
public class ClientTest extends ClientTestSupport {

  @Rule
  public TemporaryFolder storage = new TemporaryFolder();

  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws IOException {

    super.setup();
    when(es.scheduleAtFixedRate(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
      .thenReturn(sf);
    when(req.remoteURI()).thenReturn(HTTP_URI_URI);
    when(req.path()).thenReturn(LOCAL_PATH);
    when(req.transferHeaders()).thenReturn(ArrayListMultimap.create());

    Path saRootPath = Paths.get(storage.getRoot().getAbsolutePath(), SA_ROOT);
    Path localFilePath = Paths.get(storage.getRoot().getAbsolutePath(), LOCAL_PATH);

    Files.createDirectory(saRootPath);

    when(resolver.resolvePath(LOCAL_PATH)).thenReturn(localFilePath.toString());


  }

  @Test
  public void testClientCorrectlyBuildsHttpRequestNoHeaders() throws IOException {

    client.handle(req, (r, s) -> {
    });

    verify(httpClient).execute(getRequest.capture(),
        ArgumentMatchers.<ResponseHandler<Boolean>>any());

    HttpGet httpGetReq = getRequest.getValue();

    assertThat(httpGetReq.getURI(), is(HTTP_URI_URI));
    assertThat(httpGetReq.getAllHeaders(), arrayWithSize(0));

  }

  @Test
  public void testClientCorrectlyBuildsHttpRequestWithHeaders() throws IOException {

    when(req.transferHeaders()).thenReturn(HEADER_MAP);

    client.handle(req, (r, s) -> {
    });

    verify(httpClient).execute(getRequest.capture(),
        ArgumentMatchers.<ResponseHandler<Boolean>>any());

    HttpGet httpGetReq = getRequest.getValue();

    assertThat(httpGetReq.getURI(), is(HTTP_URI_URI));
    assertThat(httpGetReq.getAllHeaders(), arrayWithSize(1));
    assertThat(httpGetReq.getHeaders(AUTHORIZATION_HEADER)[0].getValue(),
        is(AUTHORIZATION_HEADER_VALUE));

  }

}
