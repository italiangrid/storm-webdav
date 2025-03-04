// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.redirector;

import static jakarta.servlet.http.HttpServletResponse.SC_TEMPORARY_REDIRECT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.redirector.RedirectFilter;
import org.italiangrid.storm.webdav.redirector.RedirectionService;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RedirectFilterTests extends RedirectorTestSupport {

  public static final String REDIRECTED_URL =
      "http://redirected.org/example/file?access_token=123456";

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  FilterChain filterChain;

  @Mock
  PathResolver pathResolver;

  @Mock
  RedirectionService redirectionService;

  @Mock
  Path path;

  @Mock
  File file;

  @Captor
  ArgumentCaptor<String> redirectUrl;

  RedirectFilter filter;

  @BeforeEach
  void setup() {

    filter = new RedirectFilter(pathResolver, redirectionService);
    lenient().when(request.getScheme()).thenReturn("https");
    lenient().when(request.getMethod()).thenReturn("GET");
    lenient().when(request.getServletPath()).thenReturn("/example/file");
    lenient().when(pathResolver.getPath("/example/file")).thenReturn(path);
    lenient().when(path.toFile()).thenReturn(file);
    lenient().when(file.isFile()).thenReturn(true);
    lenient().when(redirectionService.buildRedirect(Mockito.any(), Mockito.any(), Mockito.any()))
      .thenReturn("http://redirected.org/example/file?access_token=123456");
  }

  @Test
  void filterIgnoresPlainHttpRequests() throws IOException, ServletException {
    when(request.getScheme()).thenReturn("http");

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  void filterIgnoresPlainDavRequests() throws IOException, ServletException {
    when(request.getScheme()).thenReturn("dav");

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  void filterIgnoresRequestWithAccessToken() throws IOException, ServletException {
    when(request.getParameter("access_token")).thenReturn(RANDOM_TOKEN_STRING);

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  void filterIgnoresRequestWithNoRedirect() throws IOException, ServletException {
    Map<String, String[]> parameterMap = new HashMap<>();
    parameterMap.put("no_redirect", new String[] {});

    when(request.getParameterMap()).thenReturn(parameterMap);

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  void filterIgnoresDirectoryRequest() throws IOException, ServletException {

    when(file.isFile()).thenReturn(false);

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  void filterIgnoresResourceNotFound() throws IOException, ServletException {

    when(pathResolver.getPath("/example/file")).thenReturn(null);

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  void filterSendsRedirect() throws IOException, ServletException {


    filter.doFilter(request, response, filterChain);
    verify(redirectionService).buildRedirect(Mockito.any(), Mockito.eq(request),
        Mockito.eq(response));
    verify(response).setStatus(SC_TEMPORARY_REDIRECT);
    verify(response).setHeader(Mockito.eq("Location"), redirectUrl.capture());

    assertThat(redirectUrl.getValue(), is(REDIRECTED_URL));
    verifyNoInteractions(filterChain);



  }



}
