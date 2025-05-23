// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.enumeration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Multimap;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushTransferTest extends TransferFilterTestSupport {

  @Override
  @BeforeEach
  public void setup() throws IOException {
    super.setup();
    lenient().when(request.getMethod()).thenReturn(COPY.name());
    lenient().when(request.getServletPath()).thenReturn(SERVLET_PATH);
    lenient().when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    lenient().when(request.getHeader(TransferConstants.DESTINATION_HEADER)).thenReturn(HTTPS_URL);
    lenient().when(request.getHeaderNames()).thenReturn(emptyEnumeration());
    lenient().when(resolver.pathExists(FULL_LOCAL_PATH)).thenReturn(true);
    lenient().when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.CLIENT_INFO_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.OVERWRITE_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.REQUIRE_CHECKSUM_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.CREDENTIAL_HEADER)).thenReturn(null);
  }

  @Test
  void pushEmptyTransferHeaders() throws IOException, ServletException {
    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    assertThat(putXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(putXferRequest.getValue().remoteURI(), is(HTTPS_URL_URI));
    assertThat(putXferRequest.getValue().overwrite(), is(true));
    assertThat(putXferRequest.getValue().verifyChecksum(), is(true));
    assertTrue(
        "Expected empty xfer headers", putXferRequest.getValue().transferHeaders().isEmpty());

    verify(response).setStatus(HttpServletResponse.SC_ACCEPTED);
  }

  @Test
  void overwriteHeaderRecognized() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.OVERWRITE_HEADER)).thenReturn("F");
    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());
    assertThat(putXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(putXferRequest.getValue().remoteURI(), is(HTTPS_URL_URI));
    assertThat("Overwrite header not recognized", putXferRequest.getValue().overwrite(), is(false));
    assertThat(putXferRequest.getValue().verifyChecksum(), is(true));
    assertTrue(
        "Expected empty xfer headers", putXferRequest.getValue().transferHeaders().isEmpty());
  }

  @Test
  void checksumRecognized() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.REQUIRE_CHECKSUM_HEADER)).thenReturn("false");
    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());
    assertThat(putXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(putXferRequest.getValue().remoteURI(), is(HTTPS_URL_URI));
    assertThat(putXferRequest.getValue().overwrite(), is(true));
    assertThat(
        "RequireChecksumVerification header not recognized",
        putXferRequest.getValue().verifyChecksum(),
        is(false));
    assertTrue(
        "Expected empty xfer headers", putXferRequest.getValue().transferHeaders().isEmpty());
  }

  @Test
  void checkTransferHeaderPassing() throws IOException, ServletException {
    when(request.getHeader(TRANSFER_HEADER_AUTHORIZATION_KEY))
        .thenReturn(TRANSFER_HEADER_AUTHORIZATION_VALUE);
    when(request.getHeader(TRANSFER_HEADER_WHATEVER_KEY))
        .thenReturn(TRANSFER_HEADER_WHATEVER_VALUE);

    when(request.getHeaderNames())
        .thenReturn(
            enumeration(
                asList(
                    TRANSFER_HEADER_AUTHORIZATION_KEY,
                    TRANSFER_HEADER_WHATEVER_KEY,
                    SCITAG_HEADER)));

    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    assertThat(putXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(putXferRequest.getValue().remoteURI(), is(HTTPS_URL_URI));
    assertThat(putXferRequest.getValue().overwrite(), is(true));
    assertThat(putXferRequest.getValue().verifyChecksum(), is(true));

    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertThat(xferHeaders.size(), is(2));
    assertThat(xferHeaders.containsKey("Authorization"), is(true));
    assertThat(
        xferHeaders.get("Authorization").iterator().next(),
        is(TRANSFER_HEADER_AUTHORIZATION_VALUE));
    assertThat(xferHeaders.containsKey("Whatever"), is(true));
    assertThat(xferHeaders.get("Whatever").iterator().next(), is(TRANSFER_HEADER_WHATEVER_VALUE));
    assertThat(xferHeaders.containsKey("SciTag"), is(false));
  }

  @Test
  void emptyTransferHeaderAreIgnored() throws IOException, ServletException {
    when(request.getHeaderNames())
        .thenReturn(enumeration(asList(TRANSFER_HEADER, TRANSFER_HEADER_WHATEVER_KEY)));

    when(request.getHeader(TRANSFER_HEADER_WHATEVER_KEY))
        .thenReturn(TRANSFER_HEADER_WHATEVER_VALUE);

    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    assertThat(putXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(putXferRequest.getValue().remoteURI(), is(HTTPS_URL_URI));
    assertThat(putXferRequest.getValue().overwrite(), is(true));
    assertThat(putXferRequest.getValue().verifyChecksum(), is(true));

    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertThat(xferHeaders.size(), is(1));

    assertThat(xferHeaders.containsKey("Whatever"), is(true));
    assertThat(xferHeaders.get("Whatever").iterator().next(), is(TRANSFER_HEADER_WHATEVER_VALUE));
  }

  @Test
  void unresolvedSourcePathFailsRequest() throws IOException, ServletException {
    when(resolver.pathExists(FULL_LOCAL_PATH)).thenReturn(false);
    filter.doFilter(request, response, chain);

    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(SC_NOT_FOUND));
    assertThat(error.getValue(), is("Local source path not found: " + SERVLET_PATH + LOCAL_PATH));
  }

  @Test
  void checkExpectContinueHeaderIsSet() throws IOException, ServletException {

    when(request.getHeader(TRANSFER_HEADER_AUTHORIZATION_KEY))
        .thenReturn(TRANSFER_HEADER_AUTHORIZATION_VALUE);
    when(request.getHeaderNames())
        .thenReturn(enumeration(asList(TRANSFER_HEADER_AUTHORIZATION_KEY)));
    when(request.getContentLength()).thenReturn(1024 * 1024 + 1);

    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertThat(xferHeaders.size(), is(2));

    assertThat(xferHeaders.containsKey(EXPECTED_HEADER), is(true));
    assertThat(xferHeaders.get(EXPECTED_HEADER).iterator().next(), is(EXPECTED_VALUE));
  }

  @Test
  void checkExpectContinueHeaderIsNotSet() throws IOException, ServletException {

    when(request.getHeader(TRANSFER_HEADER_AUTHORIZATION_KEY))
        .thenReturn(TRANSFER_HEADER_AUTHORIZATION_VALUE);
    when(request.getHeaderNames())
        .thenReturn(enumeration(asList(TRANSFER_HEADER_AUTHORIZATION_KEY)));
    when(request.getContentLength()).thenReturn(1024 * 1024 - 1);

    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertThat(xferHeaders.size(), is(1));

    assertThat(xferHeaders.containsKey(EXPECTED_HEADER), is(false));
  }

  @Test
  void bothSciTagAndTransferHeaderSciTag() throws IOException, ServletException {
    when(request.getHeaderNames())
        .thenReturn(enumeration(asList(SCITAG_HEADER, TRANSFER_HEADER_SCITAG)));

    when(request.getHeader(SCITAG_HEADER)).thenReturn(SCITAG_HEADER_VALUE);

    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    assertThat(putXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(putXferRequest.getValue().remoteURI(), is(HTTPS_URL_URI));
    assertThat(putXferRequest.getValue().overwrite(), is(true));
    assertThat(putXferRequest.getValue().verifyChecksum(), is(true));

    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertThat(xferHeaders.size(), is(0));

    assertThat(xferHeaders.containsKey("SciTag"), is(false));
  }
}
