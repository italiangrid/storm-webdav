// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.italiangrid.storm.webdav.authn.PrincipalHelper;
import org.italiangrid.storm.webdav.server.tracing.RequestIdHolder;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// ForwardedHeaderFilter does not parse the "by" part of the Forwarded header
// This wraps the request so the local address/port are correct
// https://github.com/spring-projects/spring-framework/issues/34654
public class PreAuthenticatedFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER = "X-request-id";
  public static final String REQUEST_ID_ATTRIBUTE_NAME = "storm.requestId";
  public static final String REMOTE_USER_ATTR_NAME = "storm.remoteUser";
  public static final String REMOTE_ADDR_NAME = "storm.remoteAddr";

  private final boolean nginxEnabled;
  private final PrincipalHelper helper;

  public PreAuthenticatedFilter(boolean nginxEnabled, PrincipalHelper principalHelper) {
    this.nginxEnabled = nginxEnabled;
    this.helper = principalHelper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    if (nginxEnabled) {
      RequestIdHolder.setRequestId(request.getHeader(REQUEST_ID_HEADER));
    }
    if (RequestIdHolder.getRequestId() == null) {
      RequestIdHolder.setRandomId();
    }
    request.setAttribute(REQUEST_ID_ATTRIBUTE_NAME, RequestIdHolder.getRequestId());
    MDC.put(REQUEST_ID_ATTRIBUTE_NAME, RequestIdHolder.getRequestId());
    request.setAttribute(REMOTE_ADDR_NAME, request.getRemoteAddr());
    Authentication authn = SecurityContextHolder.getContext().getAuthentication();
    request.setAttribute(REMOTE_USER_ATTR_NAME, helper.getPrincipalAsString(authn));
    try {
      filterChain.doFilter(request, response);
    } finally {
      RequestIdHolder.cleanup();
      MDC.clear();
    }
  }
}
