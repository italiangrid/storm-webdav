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
import jakarta.servlet.http.HttpServletResponse;
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
class PushTransferTest extends TransferFilterTestSupport {

  @Override
  @BeforeEach
  public void setup() throws IOException {
    super.setup();
    lenient().when(request.getMethod()).thenReturn(WebDAVMethod.COPY.name());
    lenient().when(request.getServletPath()).thenReturn(SERVLET_PATH);
    lenient().when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    lenient().when(request.getHeader(TransferConstants.DESTINATION_HEADER)).thenReturn(HTTPS_URL);
    lenient().when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    lenient().when(resolver.pathExists(FULL_LOCAL_PATH)).thenReturn(true);
    lenient().when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.CLIENT_INFO_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.OVERWRITE_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.REPR_DIGEST_HEADER)).thenReturn(null);
    lenient().when(request.getHeader(TransferConstants.CREDENTIAL_HEADER)).thenReturn(null);
  }

  @Test
  void pushEmptyTransferHeaders() throws IOException, ServletException {
    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    assertEquals(FULL_LOCAL_PATH, putXferRequest.getValue().path());
    assertEquals(HTTPS_URL_URI, putXferRequest.getValue().remoteURI());
    assertTrue(putXferRequest.getValue().overwrite());
    assertEquals(Optional.empty(), putXferRequest.getValue().expectedChecksum());
    assertTrue(
        putXferRequest.getValue().transferHeaders().isEmpty(), "Expected empty xfer headers");

    verify(response).setStatus(HttpServletResponse.SC_ACCEPTED);
  }

  @Test
  void overwriteHeaderRecognized() throws IOException, ServletException {
    when(request.getHeader(TransferConstants.OVERWRITE_HEADER)).thenReturn("F");
    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());
    assertEquals(FULL_LOCAL_PATH, putXferRequest.getValue().path());
    assertEquals(HTTPS_URL_URI, putXferRequest.getValue().remoteURI());
    assertFalse(putXferRequest.getValue().overwrite(), "Overwrite header not recognized");
    assertEquals(Optional.empty(), putXferRequest.getValue().expectedChecksum());
    assertTrue(
        putXferRequest.getValue().transferHeaders().isEmpty(), "Expected empty xfer headers");
  }

  @Test
  void checksumRecognized() throws IOException, ServletException {
    when(request.getHeaderNames())
        .thenReturn(Collections.enumeration(Arrays.asList(TransferConstants.REPR_DIGEST_HEADER)));
    when(request.getHeader(TransferConstants.REPR_DIGEST_HEADER))
        .thenReturn("adler=:MDNmYzAxOWQ=:");
    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());
    assertEquals(FULL_LOCAL_PATH, putXferRequest.getValue().path());
    assertEquals(HTTPS_URL_URI, putXferRequest.getValue().remoteURI());
    assertTrue(putXferRequest.getValue().overwrite());
    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertEquals(1, xferHeaders.size());
    assertTrue(xferHeaders.containsKey(TransferConstants.REPR_DIGEST_HEADER));
    assertEquals(
        "adler=:MDNmYzAxOWQ=:",
        xferHeaders.get(TransferConstants.REPR_DIGEST_HEADER).iterator().next());
  }

  @Test
  void checkTransferHeaderPassing() throws IOException, ServletException {
    when(request.getHeader(TRANSFER_HEADER_AUTHORIZATION_KEY))
        .thenReturn(TRANSFER_HEADER_AUTHORIZATION_VALUE);
    when(request.getHeader(TRANSFER_HEADER_WHATEVER_KEY))
        .thenReturn(TRANSFER_HEADER_WHATEVER_VALUE);

    when(request.getHeaderNames())
        .thenReturn(
            Collections.enumeration(
                Arrays.asList(
                    TRANSFER_HEADER_AUTHORIZATION_KEY,
                    TRANSFER_HEADER_WHATEVER_KEY,
                    SCITAG_HEADER)));

    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    assertEquals(FULL_LOCAL_PATH, putXferRequest.getValue().path());
    assertEquals(HTTPS_URL_URI, putXferRequest.getValue().remoteURI());
    assertTrue(putXferRequest.getValue().overwrite());
    assertEquals(Optional.empty(), putXferRequest.getValue().expectedChecksum());

    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertEquals(2, xferHeaders.size());
    assertTrue(xferHeaders.containsKey("Authorization"));
    assertEquals(
        TRANSFER_HEADER_AUTHORIZATION_VALUE, xferHeaders.get("Authorization").iterator().next());
    assertTrue(xferHeaders.containsKey("Whatever"));
    assertEquals(TRANSFER_HEADER_WHATEVER_VALUE, xferHeaders.get("Whatever").iterator().next());
    assertFalse(xferHeaders.containsKey("SciTag"));
  }

  @Test
  void emptyTransferHeaderAreIgnored() throws IOException, ServletException {
    when(request.getHeaderNames())
        .thenReturn(
            Collections.enumeration(Arrays.asList(TRANSFER_HEADER, TRANSFER_HEADER_WHATEVER_KEY)));

    when(request.getHeader(TRANSFER_HEADER_WHATEVER_KEY))
        .thenReturn(TRANSFER_HEADER_WHATEVER_VALUE);

    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    assertEquals(FULL_LOCAL_PATH, putXferRequest.getValue().path());
    assertEquals(HTTPS_URL_URI, putXferRequest.getValue().remoteURI());
    assertTrue(putXferRequest.getValue().overwrite());
    assertEquals(Optional.empty(), putXferRequest.getValue().expectedChecksum());

    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertEquals(1, xferHeaders.size());

    assertTrue(xferHeaders.containsKey("Whatever"));
    assertEquals(TRANSFER_HEADER_WHATEVER_VALUE, xferHeaders.get("Whatever").iterator().next());
  }

  @Test
  void unresolvedSourcePathFailsRequest() throws IOException, ServletException {
    when(resolver.pathExists(FULL_LOCAL_PATH)).thenReturn(false);
    filter.doFilter(request, response, chain);

    verify(response).sendError(httpStatus.capture(), error.capture());
    assertEquals(HttpServletResponse.SC_NOT_FOUND, httpStatus.getValue());
    assertEquals("Local source path not found: " + SERVLET_PATH + LOCAL_PATH, error.getValue());
  }

  @Test
  void checkExpectContinueHeaderIsSet() throws IOException, ServletException {

    when(request.getHeader(TRANSFER_HEADER_AUTHORIZATION_KEY))
        .thenReturn(TRANSFER_HEADER_AUTHORIZATION_VALUE);
    when(request.getHeaderNames())
        .thenReturn(Collections.enumeration(Arrays.asList(TRANSFER_HEADER_AUTHORIZATION_KEY)));
    when(request.getContentLength()).thenReturn(1024 * 1024 + 1);

    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertEquals(2, xferHeaders.size());

    assertTrue(xferHeaders.containsKey(EXPECTED_HEADER));
    assertEquals(EXPECTED_VALUE, xferHeaders.get(EXPECTED_HEADER).iterator().next());
  }

  @Test
  void checkExpectContinueHeaderIsNotSet() throws IOException, ServletException {

    when(request.getHeader(TRANSFER_HEADER_AUTHORIZATION_KEY))
        .thenReturn(TRANSFER_HEADER_AUTHORIZATION_VALUE);
    when(request.getHeaderNames())
        .thenReturn(Collections.enumeration(Arrays.asList(TRANSFER_HEADER_AUTHORIZATION_KEY)));
    when(request.getContentLength()).thenReturn(1024 * 1024 - 1);

    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertEquals(1, xferHeaders.size());

    assertFalse(xferHeaders.containsKey(EXPECTED_HEADER));
  }

  @Test
  void bothSciTagAndTransferHeaderSciTag() throws IOException, ServletException {
    when(request.getHeaderNames())
        .thenReturn(Collections.enumeration(Arrays.asList(SCITAG_HEADER, TRANSFER_HEADER_SCITAG)));

    when(request.getHeader(SCITAG_HEADER)).thenReturn(SCITAG_HEADER_VALUE);

    filter.doFilter(request, response, chain);
    verify(client).handle(putXferRequest.capture(), Mockito.any());

    assertEquals(FULL_LOCAL_PATH, putXferRequest.getValue().path());
    assertEquals(HTTPS_URL_URI, putXferRequest.getValue().remoteURI());
    assertTrue(putXferRequest.getValue().overwrite());
    assertEquals(Optional.empty(), putXferRequest.getValue().expectedChecksum());

    Multimap<String, String> xferHeaders = putXferRequest.getValue().transferHeaders();
    assertEquals(0, xferHeaders.size());

    assertFalse(xferHeaders.containsKey("SciTag"));
  }
}
