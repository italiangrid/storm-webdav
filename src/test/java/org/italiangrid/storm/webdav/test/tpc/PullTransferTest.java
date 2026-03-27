// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Multimap;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.italiangrid.storm.webdav.server.servlet.WebDAVMethod;
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
    lenient().when(request.getMethod()).thenReturn(WebDAVMethod.COPY.name());
    lenient().when(request.getServletPath()).thenReturn(SERVLET_PATH);
    lenient().when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    lenient().when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(HTTP_URL);
    lenient().when(request.getHeader(TransferConstants.OVERWRITE_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.DESTINATION_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.CLIENT_INFO_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.CREDENTIAL_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.REPR_DIGEST_HEADER)).thenReturn(null);
    lenient().when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    lenient().when(resolver.pathExists(FULL_LOCAL_PATH)).thenReturn(false);
    lenient().when(resolver.pathExists(FULL_LOCAL_PATH_PARENT)).thenReturn(true);
  }

  @Test
  void pullEmptyTransferHeaders() throws IOException, ServletException {
    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());
    assertEquals(FULL_LOCAL_PATH, getXferRequest.getValue().path());
    assertEquals(HTTP_URL_URI, getXferRequest.getValue().remoteURI());
    assertTrue(getXferRequest.getValue().overwrite());
    assertEquals(Optional.empty(), getXferRequest.getValue().expectedChecksum());
    assertTrue(
        getXferRequest.getValue().transferHeaders().isEmpty(), "Expected empty xfer headers");
  }

  @Test
  void overwriteHeaderRecognized() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.OVERWRITE_HEADER)).thenReturn("F");
    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());
    assertEquals(FULL_LOCAL_PATH, getXferRequest.getValue().path());
    assertEquals(HTTP_URL_URI, getXferRequest.getValue().remoteURI());
    assertFalse(getXferRequest.getValue().overwrite(), "Overwrite header not recognized");
    assertEquals(Optional.empty(), getXferRequest.getValue().expectedChecksum());
    assertTrue(
        getXferRequest.getValue().transferHeaders().isEmpty(), "Expected empty xfer headers");
  }

  @Test
  void checksumRecognized() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.REPR_DIGEST_HEADER))
        .thenReturn("adler=:MDNmYzAxOWQ=:");
    when(request.getHeaderNames())
        .thenReturn(Collections.enumeration(Arrays.asList(TransferConstants.REPR_DIGEST_HEADER)));
    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());
    assertEquals(FULL_LOCAL_PATH, getXferRequest.getValue().path());
    assertEquals(HTTP_URL_URI, getXferRequest.getValue().remoteURI());
    assertTrue(getXferRequest.getValue().overwrite());
    assertEquals(Optional.of("03fc019d"), getXferRequest.getValue().expectedChecksum());
    Multimap<String, String> xferHeaders = getXferRequest.getValue().transferHeaders();
    assertEquals(1, xferHeaders.size());
    assertTrue(xferHeaders.containsKey(TransferConstants.WANT_REPR_DIGEST_HEADER));
    assertEquals(
        TransferConstants.WANT_REPR_DIGEST_HEADER_VALUE,
        xferHeaders.get(TransferConstants.WANT_REPR_DIGEST_HEADER).iterator().next());
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
            Collections.enumeration(
                Arrays.asList(
                    TRANSFER_HEADER_AUTHORIZATION_KEY,
                    TRANSFER_HEADER_WHATEVER_KEY,
                    TRANSFER_HEADER_SCITAG)));

    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());

    assertEquals(FULL_LOCAL_PATH, getXferRequest.getValue().path());
    assertEquals(HTTP_URL_URI, getXferRequest.getValue().remoteURI());
    assertTrue(getXferRequest.getValue().overwrite());
    assertEquals(Optional.empty(), getXferRequest.getValue().expectedChecksum());

    Multimap<String, String> xferHeaders = getXferRequest.getValue().transferHeaders();
    assertEquals(3, xferHeaders.size());
    assertTrue(xferHeaders.containsKey("Authorization"));
    assertEquals(
        TRANSFER_HEADER_AUTHORIZATION_VALUE, xferHeaders.get("Authorization").iterator().next());
    assertTrue(xferHeaders.containsKey("Whatever"));
    assertEquals(TRANSFER_HEADER_WHATEVER_VALUE, xferHeaders.get("Whatever").iterator().next());
    assertTrue(xferHeaders.containsKey("SciTag"));
    assertEquals(SCITAG_HEADER_VALUE, xferHeaders.get("SciTag").iterator().next());
  }

  @Test
  void emptyTransferHeaderAreIgnored() throws IOException, ServletException {
    when(request.getHeaderNames())
        .thenReturn(
            Collections.enumeration(Arrays.asList(TRANSFER_HEADER, TRANSFER_HEADER_WHATEVER_KEY)));

    when(request.getHeader(TRANSFER_HEADER_WHATEVER_KEY))
        .thenReturn(TRANSFER_HEADER_WHATEVER_VALUE);

    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());

    assertEquals(FULL_LOCAL_PATH, getXferRequest.getValue().path());
    assertEquals(HTTP_URL_URI, getXferRequest.getValue().remoteURI());
    assertTrue(getXferRequest.getValue().overwrite());
    assertEquals(Optional.empty(), getXferRequest.getValue().expectedChecksum());

    Multimap<String, String> xferHeaders = getXferRequest.getValue().transferHeaders();
    assertEquals(1, xferHeaders.size());

    assertTrue(xferHeaders.containsKey("Whatever"));
    assertEquals(TRANSFER_HEADER_WHATEVER_VALUE, xferHeaders.get("Whatever").iterator().next());
  }

  @Test
  void bothSciTagAndTransferHeaderSciTag() throws IOException, ServletException {
    when(request.getHeaderNames())
        .thenReturn(Collections.enumeration(Arrays.asList(SCITAG_HEADER, TRANSFER_HEADER_SCITAG)));

    when(request.getHeader(SCITAG_HEADER)).thenReturn(SCITAG_HEADER_VALUE);

    filter.doFilter(request, response, chain);
    verify(client).handle(getXferRequest.capture(), Mockito.any());

    assertEquals(FULL_LOCAL_PATH, getXferRequest.getValue().path());
    assertEquals(HTTP_URL_URI, getXferRequest.getValue().remoteURI());
    assertTrue(getXferRequest.getValue().overwrite());
    assertEquals(Optional.empty(), getXferRequest.getValue().expectedChecksum());

    Multimap<String, String> xferHeaders = getXferRequest.getValue().transferHeaders();
    assertEquals(0, xferHeaders.size());

    assertFalse(xferHeaders.containsKey("SciTag"));
  }
}
