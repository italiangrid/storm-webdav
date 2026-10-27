// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.redirector;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    if (isRedirectable(req)) {

      res.setHeader(
          LOCATION,
          service.buildRedirect(SecurityContextHolder.getContext().getAuthentication(), req, res));
      res.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);

    } else {
      chain.doFilter(request, response);
    }
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

    return isSecureRequest(req)
        && !isNoRedirectRequest(req)
        && isGetOrPutRequest(req)
        && requestDoesNotHaveAccessToken(req)
        && requestedResourceExistsAndIsAFile(req);
  }
}
