// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.util;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class ReadonlyHttpMethodMatcher implements RequestMatcher {

  private static final Set<String> METHODS =
      new TreeSet<>(Arrays.asList("GET", "HEAD", "OPTIONS", "PROPFIND"));

  private final RequestMatcher delegate;

  public ReadonlyHttpMethodMatcher(String pattern) {
    RequestMatcher methodMatcher = new CustomHttpMethodMatcher(METHODS);
    RequestMatcher pathMatcher;

    if (pattern.isEmpty()) {
      pathMatcher = new EmptyPathRequestMatcher();
    } else {
      pathMatcher = new AntPathRequestMatcher(pattern);
    }

    delegate = new AndRequestMatcher(pathMatcher, methodMatcher);
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    return delegate.matches(request);
  }

  @Override
  public String toString() {
    return "ReadonlyHttpMethodMatcher [delegate=" + delegate + "]";
  }

}
