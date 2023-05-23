/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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
package org.italiangrid.storm.webdav.macaroon;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MacaroonRequestFilter implements Filter {

  public static final Logger LOG = LoggerFactory.getLogger(MacaroonRequestFilter.class);

  public static final String MACAROON_REQUEST_CONTENT_TYPE = "application/macaroon-request";

  private final ObjectMapper mapper;
  private final MacaroonIssuerService service;

  public MacaroonRequestFilter(ObjectMapper mapper, MacaroonIssuerService service) {
    this.mapper = mapper;
    this.service = service;
  }

  protected boolean isMacaroonRequest(HttpServletRequest request) {
    return (MACAROON_REQUEST_CONTENT_TYPE.equals(request.getContentType()));
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if (isMacaroonRequest(httpRequest)) {
      processMacaroonRequest(httpRequest, httpResponse);
    } else {
      chain.doFilter(request, response);
    }
  }

  private void processMacaroonRequest(HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) throws IOException {

    if (!httpRequest.getMethod().equals(HttpMethod.POST.name())) {
      httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
          "Invalid macaroon request method: " + httpRequest.getMethod());
      return;
    }

    try {

      SecurityContext context = SecurityContextHolder.getContext();

      MacaroonRequestDTO req = mapper.readValue(httpRequest.getReader(), MacaroonRequestDTO.class);
      MacaroonResponseDTO res = service.createAccessToken(req, context.getAuthentication());

      httpResponse.setStatus(SC_OK);
      mapper.writeValue(httpResponse.getWriter(), res);

    } catch (AccessDeniedException e) {
      httpResponse.sendError(SC_FORBIDDEN,
          "Access denied");
      
    } catch (IOException e) {
      httpResponse.sendError(SC_BAD_REQUEST,
          "Invalid macaroon request");
    }
  }
}
