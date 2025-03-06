// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static java.util.Collections.emptyEnumeration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferRequestValidationTest extends TransferFilterTestSupport {

  @Override
  @BeforeEach
  public void setup() throws IOException {
    super.setup();
    lenient().when(request.getServletPath()).thenReturn(SERVLET_PATH);
    lenient().when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    lenient().when(request.getMethod()).thenReturn(COPY.name());
    lenient().when(request.getHeaderNames()).thenReturn(emptyEnumeration());
    lenient().when(resolver.pathExists(FULL_LOCAL_PATH)).thenReturn(false);
    lenient().when(resolver.pathExists(FULL_LOCAL_PATH_PARENT)).thenReturn(true);
    lenient().when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.OVERWRITE_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.DESTINATION_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.CLIENT_INFO_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.CREDENTIAL_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.REQUIRE_CHECKSUM_HEADER)).thenReturn(null);
  }

  @Test
  void sourceAndDestHeaderPresent() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(HTTP_URL);
    when(request.getHeader(TransferConstants.DESTINATION_HEADER)).thenReturn(HTTP_URL);

    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
    assertThat(error.getValue(), containsString("both present"));
  }

  @Test
  void invalidDestinationURIs() throws IOException, ServletException {
    for (String u : INVALID_URLs) {
      when(request.getHeader(TransferConstants.DESTINATION_HEADER)).thenReturn(u);
      filter.doFilter(request, response, chain);
      verify(response).sendError(httpStatus.capture(), error.capture());
      assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
      assertThat(error.getValue(), containsString("Invalid Destination header"));
      reset(response);
    }
  }

  @Test
  void invalidSourceURIs() throws IOException, ServletException {
    for (String u : INVALID_URLs) {
      when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(u);
      filter.doFilter(request, response, chain);
      verify(response).sendError(httpStatus.capture(), error.capture());
      assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
      assertThat(error.getValue(), containsString("Invalid Source header"));
      reset(response);
    }
  }

  @Test
  void invalidOverwriteHeader() throws IOException, ServletException {

    String[] invalidValues = {"", "cccc", "whatever", "true", "false"};

    when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(HTTP_URL);

    for (String s : invalidValues) {
      when(request.getHeader(TransferConstants.OVERWRITE_HEADER)).thenReturn(s);
      filter.doFilter(request, response, chain);
      verify(response).sendError(httpStatus.capture(), error.capture());
      assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
      assertThat(error.getValue(), containsString("Invalid Overwrite header"));
      reset(response);
    }
  }

  @Test
  void invalidRequireChecksumHeader() throws IOException, ServletException {
    String[] invalidValues = {"t", "F", ""};

    when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(HTTP_URL);
    for (String s : invalidValues) {
      when(request.getHeader(TransferConstants.REQUIRE_CHECKSUM_HEADER)).thenReturn(s);
      filter.doFilter(request, response, chain);
      verify(response).sendError(httpStatus.capture(), error.capture());
      assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
      assertThat(error.getValue(), containsString("Invalid RequireChecksumVerification header"));
      reset(response);
    }
  }

  @Test
  void emptyOrNullPathInfo() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(HTTP_URL);

    String[] invalidPathInfos = {null, "", "does/not/start/with/slash"};
    String[] expectedErrorMsgs = {"Null or empty", "Null or empty", "Invalid local path"};

    for (int i = 0; i < invalidPathInfos.length; i++) {
      when(request.getPathInfo()).thenReturn(invalidPathInfos[i]);

      filter.doFilter(request, response, chain);
      verify(response).sendError(httpStatus.capture(), error.capture());
      assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
      assertThat(error.getValue(), containsString(expectedErrorMsgs[i]));
      reset(response);
    }
  }

  @Test
  void invalidCredentialHeader() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(HTTP_URL);
    when(request.getHeader(TransferConstants.CREDENTIAL_HEADER)).thenReturn("gridsite");
    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(SC_BAD_REQUEST));
    assertThat(error.getValue(), is("Unsupported Credential header value: gridsite"));
  }

  @Test
  void noneCredentialHeaderAccepted() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(HTTP_URL);
    when(request.getHeader(TransferConstants.CREDENTIAL_HEADER)).thenReturn("none");
    filter.doFilter(request, response, chain);
    verify(response).setStatus(httpStatus.capture());

    assertThat(httpStatus.getValue(), is(HttpServletResponse.SC_ACCEPTED));
  }
}
