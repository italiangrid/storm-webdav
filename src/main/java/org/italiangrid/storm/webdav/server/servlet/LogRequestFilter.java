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
package org.italiangrid.storm.webdav.server.servlet;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class LogRequestFilter implements Filter {

  public static final Logger log = LoggerFactory.getLogger(LogRequestFilter.class);

  @Override
  public void destroy() {}

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

    String resMsg = String.format("%s %s %s %d [user:<%s>, authorities:<%s>]", req.getRemoteAddr(),
        req.getMethod(), req.getRequestURI(), res.getStatus(),
        authn.isPresent() ? authn.get().getName() : null,
        authn.isPresent() ? authn.get().getAuthorities() : null);

    log.debug(resMsg);
  }

  @Override
  public void init(FilterConfig config) throws ServletException {}

}
