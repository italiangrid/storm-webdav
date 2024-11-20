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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.italiangrid.storm.webdav.tpc.http.GetResponseHandler;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetResponseHandlerTest extends ClientTestSupport {

  @Mock
  HttpEntity entity;

  @Mock
  ClassicHttpResponse response;

  @Mock
  StormCountingOutputStream os;

  GetResponseHandler handler;

  @Override
  @BeforeEach
  public void setup() {

    handler = new GetResponseHandler(null, os, eah);
    lenient().when(response.getEntity()).thenReturn(entity);
  }

  @Test
  void handlerWritesToStream() throws IOException {
    handler.handleResponse(response);

    verify(entity).getContent();
    verify(eah).setChecksumAttribute(ArgumentMatchers.<Path>any(), any());
  }
}
