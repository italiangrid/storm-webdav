// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

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
import java.io.IOException;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PullTransferTest extends TransferFilterTestSupport {

  @Override
  @BeforeEach
  public void setup() throws IOException {
    super.setup();
    lenient().when(request.getMethod()).thenReturn(COPY.name());
    lenient().when(request.getServletPath()).thenReturn(SERVLET_PATH);
    lenient().when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    lenient().when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(HTTP_URL);
    lenient().when(request.getHeader(TransferConstants.OVERWRITE_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.DESTINATION_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.CLIENT_INFO_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.CREDENTIAL_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.REQUIRE_CHECKSUM_HEADER)).thenReturn(null);
    lenient().when(request.getHeaderNames()).thenReturn(emptyEnumeration());
    lenient().when(resolver.pathExists(FULL_LOCAL_PATH)).thenReturn(false);
    lenient().when(resolver.pathExists(FULL_LOCAL_PATH_PARENT)).thenReturn(true);
  }

  @Test
  void pullEmptyTransferHeaders() throws IOException, ServletException {
    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());
    assertThat(getXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(getXferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat(getXferRequest.getValue().overwrite(), is(true));
    assertThat(getXferRequest.getValue().verifyChecksum(), is(true));
    assertTrue(
        "Expected empty xfer headers", getXferRequest.getValue().transferHeaders().isEmpty());
  }

  @Test
  void overwriteHeaderRecognized() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.OVERWRITE_HEADER)).thenReturn("F");
    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());
    assertThat(getXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(getXferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat("Overwrite header not recognized", getXferRequest.getValue().overwrite(), is(false));
    assertThat(getXferRequest.getValue().verifyChecksum(), is(true));
    assertTrue(
        "Expected empty xfer headers", getXferRequest.getValue().transferHeaders().isEmpty());
  }

  @Test
  void checksumRecognized() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.REQUIRE_CHECKSUM_HEADER)).thenReturn("false");
    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());
    assertThat(getXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(getXferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat(getXferRequest.getValue().overwrite(), is(true));
    assertThat(
        "RequireChecksumVerification header not recognized",
        getXferRequest.getValue().verifyChecksum(),
        is(false));
    assertTrue(
        "Expected empty xfer headers", getXferRequest.getValue().transferHeaders().isEmpty());
  }

  @Test
  void checkTransferHeaderPassing() throws IOException, ServletException {
    when(request.getHeader(TRANSFER_HEADER_AUTHORIZATION_KEY))
        .thenReturn(TRANSFER_HEADER_AUTHORIZATION_VALUE);
    when(request.getHeader(TRANSFER_HEADER_WHATEVER_KEY))
        .thenReturn(TRANSFER_HEADER_WHATEVER_VALUE);
    when(request.getHeader(TRANSFER_HEADER_SCITAG)).thenReturn(SCITAG_HEADER_VALUE);
    when(request.getHeader(SCITAG_HEADER)).thenReturn(null);

    when(request.getHeaderNames())
        .thenReturn(
            enumeration(
                asList(
                    TRANSFER_HEADER_AUTHORIZATION_KEY,
                    TRANSFER_HEADER_WHATEVER_KEY,
                    TRANSFER_HEADER_SCITAG)));

    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());

    assertThat(getXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(getXferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat(getXferRequest.getValue().overwrite(), is(true));
    assertThat(getXferRequest.getValue().verifyChecksum(), is(true));

    Multimap<String, String> xferHeaders = getXferRequest.getValue().transferHeaders();
    assertThat(xferHeaders.size(), is(3));
    assertThat(xferHeaders.containsKey("Authorization"), is(true));
    assertThat(
        xferHeaders.get("Authorization").iterator().next(),
        is(TRANSFER_HEADER_AUTHORIZATION_VALUE));
    assertThat(xferHeaders.containsKey("Whatever"), is(true));
    assertThat(xferHeaders.get("Whatever").iterator().next(), is(TRANSFER_HEADER_WHATEVER_VALUE));
    assertThat(xferHeaders.containsKey("SciTag"), is(true));
    assertThat(xferHeaders.get("SciTag").iterator().next(), is(SCITAG_HEADER_VALUE));
  }

  @Test
  void emptyTransferHeaderAreIgnored() throws IOException, ServletException {
    when(request.getHeaderNames())
        .thenReturn(enumeration(asList(TRANSFER_HEADER, TRANSFER_HEADER_WHATEVER_KEY)));

    when(request.getHeader(TRANSFER_HEADER_WHATEVER_KEY))
        .thenReturn(TRANSFER_HEADER_WHATEVER_VALUE);

    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());

    assertThat(getXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(getXferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat(getXferRequest.getValue().overwrite(), is(true));
    assertThat(getXferRequest.getValue().verifyChecksum(), is(true));

    Multimap<String, String> xferHeaders = getXferRequest.getValue().transferHeaders();
    assertThat(xferHeaders.size(), is(1));

    assertThat(xferHeaders.containsKey("Whatever"), is(true));
    assertThat(xferHeaders.get("Whatever").iterator().next(), is(TRANSFER_HEADER_WHATEVER_VALUE));
  }

  @Test
  void bothSciTagAndTransferHeaderSciTag() throws IOException, ServletException {
    when(request.getHeaderNames())
        .thenReturn(enumeration(asList(SCITAG_HEADER, TRANSFER_HEADER_SCITAG)));

    when(request.getHeader(SCITAG_HEADER)).thenReturn(SCITAG_HEADER_VALUE);

    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());

    assertThat(getXferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(getXferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat(getXferRequest.getValue().overwrite(), is(true));
    assertThat(getXferRequest.getValue().verifyChecksum(), is(true));

    Multimap<String, String> xferHeaders = getXferRequest.getValue().transferHeaders();
    assertThat(xferHeaders.size(), is(0));

    assertThat(xferHeaders.containsKey("SciTag"), is(false));
  }
}
