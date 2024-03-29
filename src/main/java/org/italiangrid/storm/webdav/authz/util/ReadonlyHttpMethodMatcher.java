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
package org.italiangrid.storm.webdav.authz.util;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

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
