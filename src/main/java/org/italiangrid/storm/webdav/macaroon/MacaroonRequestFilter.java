// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.macaroon;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class MacaroonRequestFilter implements Filter {

  public static final Logger LOG = LoggerFactory.getLogger(MacaroonRequestFilter.class);

  public static final String MACAROON_REQUEST_CONTENT_TYPE = "application/macaroon-request";

  private final ObjectMapper mapper;
  private final MacaroonIssuerService service;

  public MacaroonRequestFilter(ObjectMapper mapper, MacaroonIssuerService service) {
    this.mapper = mapper;
    this.service = service;
  }

  public static boolean isMacaroonRequest(HttpServletRequest request) {
    return (MACAROON_REQUEST_CONTENT_TYPE.equals(request.getContentType()));
  }

  @WithSpan
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

  @WithSpan
  private void processMacaroonRequest(
      HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

    if (!httpRequest.getMethod().equals(HttpMethod.POST.name())) {
      httpResponse.sendError(
          HttpServletResponse.SC_METHOD_NOT_ALLOWED,
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
      httpResponse.sendError(SC_FORBIDDEN, "Access denied");
    } catch (IOException e) {
      httpResponse.sendError(SC_BAD_REQUEST, "Invalid macaroon request");
    }
  }
}
