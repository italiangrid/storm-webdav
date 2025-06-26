// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.lenient;

import jakarta.servlet.http.HttpServletRequest;
import org.italiangrid.storm.webdav.server.servlet.WebDAVMethod;
import org.italiangrid.storm.webdav.tpc.SwappedServletRequest;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
class SwappedServletRequestTest {

  @Mock HttpServletRequest request;

  final String scheme = "https";
  final String serverName = "storm.example";
  final int port = 8443;
  final String destinationPath = "/sa2/destination";
  final String sourcePath = "/sa1/source";

  @Test
  void localCopyAcrossStorageAreasWithRelativeSourcePath() {
    lenient()
        .when(request.getRequestURL())
        .thenReturn(new StringBuffer(scheme + "://" + serverName + ":" + port + destinationPath));
    lenient().when(request.getMethod()).thenReturn(WebDAVMethod.COPY.name());
    lenient().when(request.getScheme()).thenReturn(scheme);
    lenient().when(request.getServerName()).thenReturn(serverName);
    lenient().when(request.getServerPort()).thenReturn(port);
    lenient().when(request.getHeader(HttpHeaders.HOST)).thenReturn(serverName + ":" + port);
    lenient().when(request.getHeader(TransferConstants.SOURCE_HEADER)).thenReturn(sourcePath);
    SwappedServletRequest swappedRequest = new SwappedServletRequest(request);
    assertThat(
        swappedRequest.getRequestURL().toString(),
        is(scheme + "://" + serverName + ":" + port + sourcePath));
    assertThat(swappedRequest.getHeader(HttpHeaders.HOST), is(serverName + ":" + port));
    assertThat(
        swappedRequest.getHeader(TransferConstants.DESTINATION_HEADER),
        is(scheme + "://" + serverName + ":" + port + destinationPath));
  }

  @Test
  void localCopyAcrossStorageAreasWithAbsoluteSourcePath() {
    lenient()
        .when(request.getRequestURL())
        .thenReturn(new StringBuffer(scheme + "://" + serverName + ":" + port + destinationPath));
    lenient().when(request.getMethod()).thenReturn(WebDAVMethod.COPY.name());
    lenient().when(request.getScheme()).thenReturn(scheme);
    lenient().when(request.getServerName()).thenReturn(serverName);
    lenient().when(request.getServerPort()).thenReturn(port);
    lenient().when(request.getHeader(HttpHeaders.HOST)).thenReturn(serverName + ":" + port);
    lenient()
        .when(request.getHeader(TransferConstants.SOURCE_HEADER))
        .thenReturn(scheme + "://" + serverName + ":" + port + sourcePath);
    SwappedServletRequest swappedRequest = new SwappedServletRequest(request);
    assertThat(
        swappedRequest.getRequestURL().toString(),
        is(scheme + "://" + serverName + ":" + port + sourcePath));
    assertThat(swappedRequest.getHeader(HttpHeaders.HOST), is(serverName + ":" + port));
    assertThat(
        swappedRequest.getHeader(TransferConstants.DESTINATION_HEADER),
        is(scheme + "://" + serverName + ":" + port + destinationPath));
  }

  @Test
  void localCopyAcrossStorageAreasWithAbsoluteSourcePathAndDifferentHost() {
    String differentServerName = "storm2.example";
    lenient()
        .when(request.getRequestURL())
        .thenReturn(new StringBuffer(scheme + "://" + serverName + ":" + port + destinationPath));
    lenient().when(request.getMethod()).thenReturn(WebDAVMethod.COPY.name());
    lenient().when(request.getScheme()).thenReturn(scheme);
    lenient().when(request.getServerName()).thenReturn(serverName);
    lenient().when(request.getServerPort()).thenReturn(port);
    lenient().when(request.getHeader(HttpHeaders.HOST)).thenReturn(serverName + ":" + port);
    lenient()
        .when(request.getHeader(TransferConstants.SOURCE_HEADER))
        .thenReturn(scheme + "://" + differentServerName + ":" + port + sourcePath);
    SwappedServletRequest swappedRequest = new SwappedServletRequest(request);
    assertThat(
        swappedRequest.getRequestURL().toString(),
        is(scheme + "://" + differentServerName + ":" + port + sourcePath));
    assertThat(swappedRequest.getHeader(HttpHeaders.HOST), is(differentServerName + ":" + port));
    assertThat(
        swappedRequest.getHeader(TransferConstants.DESTINATION_HEADER),
        is(scheme + "://" + serverName + ":" + port + destinationPath));
  }
}
