// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
