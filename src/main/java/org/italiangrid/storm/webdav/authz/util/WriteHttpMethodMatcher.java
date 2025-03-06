// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.util;

import static java.util.Arrays.asList;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class WriteHttpMethodMatcher implements RequestMatcher {

  // COPY is now classified as a write method since there's a single case
  // where COPY is a read-only method: TPC push copy, while local COPY (in the same storage
  // area) and TPC pull COPY all require write privileges
  private static final Set<String> METHODS = new TreeSet<>(asList("PUT", "MKCOL", "MOVE", "COPY"));

  private final RequestMatcher delegate;

  public WriteHttpMethodMatcher(String pattern) {
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
    return "WriteHttpMethodMatcher [delegate=" + delegate + "]";
  }
}
