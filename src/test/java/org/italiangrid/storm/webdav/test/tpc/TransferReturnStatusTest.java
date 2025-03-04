// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static java.util.Collections.emptyEnumeration;
import static jakarta.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static jakarta.servlet.http.HttpServletResponse.SC_PRECONDITION_FAILED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.HttpResponseException;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.italiangrid.storm.webdav.tpc.transfer.error.TransferError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferReturnStatusTest extends TransferFilterTestSupport {

  @Override
  @BeforeEach
  public void setup() throws IOException {
    super.setup();
    when(request.getMethod()).thenReturn(COPY.name());
    when(request.getServletPath()).thenReturn(SERVLET_PATH);
    when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(HTTP_URL);
    when(request.getHeaderNames()).thenReturn(emptyEnumeration());
    when(resolver.pathExists(FULL_LOCAL_PATH_PARENT)).thenReturn(true);
  }

  @Test
  void filterAnswers202() throws IOException, ServletException {
    filter.doFilter(request, response, chain);
    verify(response).setStatus(httpStatus.capture());
    assertThat(httpStatus.getValue(), is(SC_ACCEPTED));
  }

  @Test
  void filterAnswers412ForClientProtocolException() throws IOException, ServletException {
    Mockito.doThrow(new ClientProtocolException("Connection error"))
      .when(client)
      .handle(ArgumentMatchers.<GetTransferRequest>any(), ArgumentMatchers.any());

    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(SC_PRECONDITION_FAILED));
    assertThat(error.getValue(), is("Third party transfer error: Connection error"));
  }

  @Test
  void filterAnswers412ForHttpExceptionError() throws IOException, ServletException {
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
  void filterAnswers412ForChecksumVerificationError() throws IOException, ServletException {
    Mockito.doThrow(new ChecksumVerificationError("Checksum verification error"))
      .when(client)
      .handle(ArgumentMatchers.<GetTransferRequest>any(), ArgumentMatchers.any());

    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(SC_PRECONDITION_FAILED));
    assertThat(error.getValue(), is("Checksum verification error"));
  }

  @Test
  void filterAnswers412ForGenericTransferError() throws IOException, ServletException {
    Mockito.doThrow(new TransferError("Error"))
      .when(client)
      .handle(ArgumentMatchers.<GetTransferRequest>any(), ArgumentMatchers.any());

    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(SC_PRECONDITION_FAILED));
    assertThat(error.getValue(), is("Error"));
  }

}
