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
package org.italiangrid.storm.webdav.redirector;

import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

public class RedirectFilter implements Filter, TpcUtils {
  public static final String LOCATION = "Location";

  public static final String NO_REDIRECT_QUERY_PARAM = "no_redirect";
  public static final Logger LOG = LoggerFactory.getLogger(RedirectFilter.class);

  private final PathResolver pathResolver;
  private final RedirectionService service;

  public RedirectFilter(PathResolver pathResolver, RedirectionService service) {
    this.pathResolver = pathResolver;
    this.service = service;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    SecurityContext context = resolveSecurityContext();

    if (isRedirectable(req)) {

      res.setHeader(LOCATION, service.buildRedirect(context.getAuthentication(), req, res));
      res.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);

    } else {
      chain.doFilter(request, response);
    }

  }

  private SecurityContext resolveSecurityContext() {
    SecurityContext context = SecurityContextHolder.getContext();

    if (context == null) {
      throw new RedirectError("Failed to enstabilish a valid security context");
    }

    return context;
  }

  private boolean isNoRedirectRequest(HttpServletRequest req) {
    return req.getParameterMap().containsKey(NO_REDIRECT_QUERY_PARAM);
  }

  private boolean isSecureRequest(HttpServletRequest req) {
    return "https".equals(req.getScheme()) || "davs".equals(req.getScheme());
  }

  private boolean isGetOrPutRequest(HttpServletRequest req) {
    return "GET".equals(req.getMethod()) || "PUT".equals(req.getMethod());
  }


  private boolean requestDoesNotHaveAccessToken(HttpServletRequest req) {
    String accessToken = req.getParameter(RedirectConstants.ACCESS_TOKEN_PARAMETER);
    return !StringUtils.hasText(accessToken);
  }


  private boolean requestedResourceExistsAndIsAFile(HttpServletRequest req) {
    String path = getSerlvetRequestPath(req);
    Path p = pathResolver.getPath(path);

    if (p == null) {
      return false;
    }

    return p.toFile().isFile();
  }

  private boolean isRedirectable(HttpServletRequest req) {

    return isSecureRequest(req) && !isNoRedirectRequest(req) && isGetOrPutRequest(req)
        && requestDoesNotHaveAccessToken(req)
        && requestedResourceExistsAndIsAFile(req);
  }

}
