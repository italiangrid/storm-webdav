// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.EnumSet;

import jakarta.servlet.ServletException;

import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.servlet.WebDAVMethod;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

@ExtendWith(MockitoExtension.class)
class TransferFilterActivationTest extends TransferFilterTestSupport {

  @Mock
  StorageAreaInfo testSa;

  @Mock
  StorageAreaInfo otherSa;

  @Override
  @BeforeEach
  public void setup() throws IOException {
    super.setup();
    lenient().when(request.getServletPath()).thenReturn(SERVLET_PATH);
    lenient().when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    lenient().when(response.getWriter()).thenReturn(responseWriter);
    lenient().when(resolver.resolveStorageArea(FULL_LOCAL_PATH)).thenReturn(testSa);
    lenient().when(resolver.resolveStorageArea("/test/otherfile")).thenReturn(testSa);
    lenient().when(resolver.resolveStorageArea("/other/file")).thenReturn(otherSa);
    lenient().when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(null);
  }

  @Test
  void filterIgnoresOtherHttpOrWebdavMethods() throws IOException, ServletException {
    // Ignore Http methods
    for (HttpMethod m : HttpMethod.values()) {
      when(request.getMethod()).thenReturn(m.toString());
      filter.doFilter(request, response, chain);
      verify(chain).doFilter(request, response);
      reset(chain);
    }

    // Ignore other WebDAV methods
    EnumSet<WebDAVMethod> nonCopyMethods = EnumSet.complementOf(EnumSet.of(WebDAVMethod.COPY));
    for (WebDAVMethod m : nonCopyMethods) {
      when(request.getMethod()).thenReturn(m.toString());
      filter.doFilter(request, response, chain);
      verify(chain).doFilter(request, response);
      reset(chain);
    }
  }

  @Test
  void filterSkippedIfSourceAndDestionationHeaderMissing() throws IOException, ServletException {

    // No source or destination header
    when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(null);
    when(request.getHeader(TransferConstants.DESTINATION_HEADER)).thenReturn(null);

    when(request.getMethod()).thenReturn(COPY.toString());
    filter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
  }

  @Test
  void filterBlocksLocalCopyAcrossStorageAreas() throws IOException, ServletException {
    when(request.getMethod()).thenReturn(COPY.toString());
    when(request.getHeader(TransferConstants.DESTINATION_HEADER))
      .thenReturn("https://localhost/other/file");
    filter.doFilter(request, response, chain);
    verify(responseWriter).print(error.capture());
    assertThat(error.getValue(), is("Local copy across storage areas is not supported"));
    verifyNoInteractions(chain);
  }

  @Test
  void filterIgnoresLocalCopyInSameStorageArea() throws IOException, ServletException {
    when(request.getMethod()).thenReturn(COPY.toString());
    when(request.getHeader(TransferConstants.DESTINATION_HEADER))
      .thenReturn("https://localhost/test/otherfile");
    filter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
  }

  @Test
  void filterHandlesLocalCopyWithTransferHeader() throws IOException, ServletException {
    when(request.getMethod()).thenReturn(COPY.toString());
    when(request.getHeader(TransferConstants.DESTINATION_HEADER))
      .thenReturn("https://localhost/test/otherfile");
    when(requestHeaderNames.hasMoreElements()).thenReturn(true, true, false);
    when(requestHeaderNames.nextElement()).thenReturn(TransferConstants.DESTINATION_HEADER,
        TRANSFER_HEADER_AUTHORIZATION_KEY);

    filter.doFilter(request, response, chain);
    verifyNoInteractions(chain);
  }

  @Test
  void filterHandlesRemoteSourceHeader() throws IOException, ServletException {
    when(request.getMethod()).thenReturn(COPY.toString());
    when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(HTTP_URL);
    filter.doFilter(request, response, chain);
    verifyNoInteractions(chain);
  }

  @Test
  void filterHandlesRemoteDestinationHeader() throws IOException, ServletException {
    when(request.getMethod()).thenReturn(COPY.toString());
    when(request.getHeader(TransferConstants.DESTINATION_HEADER)).thenReturn(HTTP_URL);
    filter.doFilter(request, response, chain);
    verifyNoInteractions(chain);
  }

}
