/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.italiangrid.storm.webdav.test.redirector;

import static javax.servlet.http.HttpServletResponse.SC_TEMPORARY_REDIRECT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.google.common.collect.Maps;

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
  public void setup() {

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
  public void filterIgnoresPlainHttpRequests() throws IOException, ServletException {
    when(request.getScheme()).thenReturn("http");

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  public void filterIgnoresPlainDavRequests() throws IOException, ServletException {
    when(request.getScheme()).thenReturn("dav");

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  public void filterIgnoresRequestWithAccessToken() throws IOException, ServletException {
    when(request.getParameter("access_token")).thenReturn(RANDOM_TOKEN_STRING);

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  public void filterIgnoresRequestWithNoRedirect() throws IOException, ServletException {
    Map<String, String[]> parameterMap = Maps.newHashMap();
    parameterMap.put("no_redirect", new String[] {});

    when(request.getParameterMap()).thenReturn(parameterMap);

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  public void filterIgnoresDirectoryRequest() throws IOException, ServletException {

    when(file.isFile()).thenReturn(false);

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  public void filterIgnoresResourceNotFound() throws IOException, ServletException {

    when(pathResolver.getPath("/example/file")).thenReturn(null);

    filter.doFilter(request, response, filterChain);
    verifyNoInteractions(redirectionService);
    verify(filterChain).doFilter(request, response);

  }

  @Test
  public void filterSendsRedirect() throws IOException, ServletException {


    filter.doFilter(request, response, filterChain);
    verify(redirectionService).buildRedirect(Mockito.any(), Mockito.eq(request),
        Mockito.eq(response));
    verify(response).setStatus(Mockito.eq(SC_TEMPORARY_REDIRECT));
    verify(response).setHeader(Mockito.eq("Location"), redirectUrl.capture());

    assertThat(redirectUrl.getValue(), is(REDIRECTED_URL));
    verifyNoInteractions(filterChain);



  }



}
