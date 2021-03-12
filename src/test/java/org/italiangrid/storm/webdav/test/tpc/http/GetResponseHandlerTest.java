/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.tpc.http.GetResponseHandler;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetResponseHandlerTest extends ClientTestSupport {

  @Mock
  StatusLine status;
  
  @Mock
  HttpEntity entity;
  
  @Mock
  HttpResponse response;
  
  @Mock
  StormCountingOutputStream os;
  
  @Mock
  ExtendedAttributesHelper ah;
  
  GetResponseHandler handler;

  @Before
  public void setup() {
    
    handler = new GetResponseHandler(null, os, ah);
    when(response.getStatusLine()).thenReturn(status);
    when(response.getEntity()).thenReturn(entity);
  }
  
  @Test
  public void handlerWritesToStream() throws IOException {
    when(status.getStatusCode()).thenReturn(200);
    
    handler.handleResponse(response);
    
    verify(entity).getContent();
    verify(ah).setChecksumAttribute(ArgumentMatchers.<Path>any(), any());
  }
}
