// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import io.opentelemetry.instrumentation.annotations.WithSpan;

public class LogRequestFilter implements Filter {

  public static final Logger log = LoggerFactory.getLogger(LogRequestFilter.class);

  private static final List<String> IP_HEADERS =
      List.of(
          "X-Forwarded-For",
          "Proxy-Client-IP",
          "WL-Proxy-Client-IP",
          "HTTP_CLIENT_IP",
          "HTTP_X_FORWARDED_FOR");

  @Override
  public void destroy() {}

  @WithSpan
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    chain.doFilter(request, response);

    Optional<SecurityContext> ctxt = Optional.ofNullable(SecurityContextHolder.getContext());
    Optional<Authentication> authn = Optional.empty();

    if (ctxt.isPresent()) {
      authn = Optional.ofNullable(ctxt.get().getAuthentication());
    }

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    String resMsg =
        String.format(
            "%s %s %s %d [user:<%s>, authorities:<%s>]",
            getClientIpAddr(req),
            req.getMethod(),
            req.getRequestURI(),
            res.getStatus(),
            authn.isPresent() ? authn.get().getName() : null,
            authn.isPresent() ? authn.get().getAuthorities() : null);

    log.debug(resMsg);
  }

  public static String getClientIpAddr(HttpServletRequest request) {

    String remoteIp = request.getRemoteAddr();
    if (remoteIp != null) {
      return remoteIp;
    }
    return IP_HEADERS.stream()
        .map(request::getHeader)
        .filter(Objects::nonNull)
        .filter(ip -> !ip.isEmpty() && !ip.equalsIgnoreCase("unknown"))
        .findFirst()
        .orElse("???.???.???.???");
  }

  @Override
  public void init(FilterConfig config) throws ServletException {}
}
