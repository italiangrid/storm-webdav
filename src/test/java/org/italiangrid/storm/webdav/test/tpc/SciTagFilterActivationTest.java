// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.scitag.SciTagTransfer;
import org.italiangrid.storm.webdav.server.servlet.SciTagFilter;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SciTagFilterActivationTest extends TransferFilterTestSupport {
  @TempDir File tempDir;

  SciTagFilter sciTagFilter;

  @Mock StorageAreaInfo testSa;

  @Mock StorageAreaInfo otherSa;

  @Override
  @BeforeEach
  public void setup() throws IOException {
    super.setup();
    sciTagFilter = new SciTagFilter();
    lenient().when(request.getServletPath()).thenReturn(SERVLET_PATH);
    lenient().when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    lenient().when(response.getWriter()).thenReturn(responseWriter);
    lenient().when(resolver.resolveStorageArea(FULL_LOCAL_PATH)).thenReturn(testSa);
    lenient().when(resolver.resolveStorageArea("/test/otherfile")).thenReturn(testSa);
    lenient().when(resolver.resolveStorageArea("/other/file")).thenReturn(otherSa);
    lenient().when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(null);
  }

  @Test
  void requestWithScitag() throws IOException, ServletException {
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeader(SCITAG_HEADER)).thenReturn("66");

    sciTagFilter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    verify(request).setAttribute(eq(SciTag.SCITAG_ATTRIBUTE), any(SciTag.class));
  }

  @Test
  void requestWithoutScitag() throws IOException, ServletException {
    sciTagFilter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    verify(request, times(0)).setAttribute(eq(SciTag.SCITAG_ATTRIBUTE), any(SciTag.class));
  }

  @Test
  void testSciTagWrite() {
    SciTag scitag = new SciTag(1, 2, false);
    assertThat(scitag.experimentId(), is(1));
    assertThat(scitag.activityId(), is(2));
    assertThat(scitag.remoteAddressIsSource(), is(false));
    File mockFile = new File(tempDir, "flowd");
    SciTagTransfer scitagTransfer =
        new SciTagTransfer(scitag, "10.10.10.10", 8443, "10.10.10.11", 12345, mockFile);
    scitagTransfer.writeStart();
    scitagTransfer.writeEnd();
  }
}
