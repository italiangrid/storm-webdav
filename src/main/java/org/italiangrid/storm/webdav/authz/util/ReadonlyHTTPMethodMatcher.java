package org.italiangrid.storm.webdav.authz.util;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class ReadonlyHTTPMethodMatcher implements RequestMatcher {

  private static final Logger logger = LoggerFactory
    .getLogger(ReadonlyHTTPMethodMatcher.class);

  private static final Set<String> METHODS = new TreeSet<String>(Arrays.asList(
    "GET", "HEAD", "OPTIONS", "PROPFIND"));

  @Override
  public boolean matches(HttpServletRequest request) {

    if (request.getMethod() == null) {
      if (logger.isDebugEnabled()) {
        logger
          .debug("null method in incoming request will not match this matcher.");
      }
      return false;
    }

    final boolean matches = METHODS.contains(request.getMethod());

    if (logger.isDebugEnabled() && !matches) {
      logger.debug("Request '{} {}' does not match with this matcher.",
        request.getMethod(), getRequestPath(request));
    }

    return matches;

  }

  private String getRequestPath(HttpServletRequest request) {

    String url = request.getServletPath();

    if (request.getPathInfo() != null) {
      url += request.getPathInfo();
    }

    return url;
  }
}
