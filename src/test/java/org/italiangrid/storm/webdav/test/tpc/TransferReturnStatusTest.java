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
package org.italiangrid.storm.webdav.test.tpc;

import static java.util.Collections.emptyEnumeration;
import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_PRECONDITION_FAILED;
import static org.hamcrest.CoreMatchers.is;
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.italiangrid.storm.webdav.tpc.transfer.error.TransferError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransferReturnStatusTest extends TransferFilterTestSupport {

  @Before
  public void setup() throws IOException {
    super.setup();
    when(request.getMethod()).thenReturn(COPY.name());
    when(request.getServletPath()).thenReturn(SERVLET_PATH);
    when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    when(request.getHeader(SOURCE_HEADER)).thenReturn(HTTP_URL);
    when(request.getHeaderNames()).thenReturn(emptyEnumeration());
    when(resolver.pathExists(FULL_LOCAL_PATH_PARENT)).thenReturn(true);
  }

  @Test
  public void filterAnswers202() throws IOException, ServletException {
    filter.doFilter(request, response, chain);
    verify(response).setStatus(httpStatus.capture());
    assertThat(httpStatus.getValue(), is(SC_ACCEPTED));
  }

  @Test
  public void filterAnswers412ForClientProtocolException() throws IOException, ServletException {
    Mockito.doThrow(new ClientProtocolException("Connection error"))
      .when(client)
      .handle(ArgumentMatchers.<GetTransferRequest>any(), ArgumentMatchers.any());

    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(SC_PRECONDITION_FAILED));
    assertThat(error.getValue(), is("Third party transfer error: Connection error"));
  }

  @Test
  public void filterAnswers412ForHttpExceptionError() throws IOException, ServletException {
    Mockito.doThrow(new HttpResponseException(HttpServletResponse.SC_FORBIDDEN, "Access denied"))
      .when(client)
      .handle(ArgumentMatchers.<GetTransferRequest>any(), ArgumentMatchers.any());

    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(SC_PRECONDITION_FAILED));
    assertThat(error.getValue(),
        is("Third party transfer error: 403 status code: 403, reason phrase: Access denied"));
  }

  @Test
  public void filterAnswers412ForChecksumVerificationError() throws IOException, ServletException {
    Mockito.doThrow(new ChecksumVerificationError("Checksum verification error"))
      .when(client)
      .handle(ArgumentMatchers.<GetTransferRequest>any(), ArgumentMatchers.any());

    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(SC_PRECONDITION_FAILED));
    assertThat(error.getValue(), is("Checksum verification error"));
  }

  @Test
  public void filterAnswers412ForGenericTransferError() throws IOException, ServletException {
    Mockito.doThrow(new TransferError("Error"))
      .when(client)
      .handle(ArgumentMatchers.<GetTransferRequest>any(), ArgumentMatchers.any());

    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(SC_PRECONDITION_FAILED));
    assertThat(error.getValue(), is("Error"));
  }

}
