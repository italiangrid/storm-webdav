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

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class CustomHttpMethodMatcher implements RequestMatcher, MatcherUtils {

  public static final Logger LOG = LoggerFactory.getLogger(CustomHttpMethodMatcher.class);

  protected final Set<String> supportedMethods;

  public CustomHttpMethodMatcher(Set<String> supportedMethods) {
    this.supportedMethods = supportedMethods;
  }

  public Set<String> getSupportedMethods() {
    return supportedMethods;
  }

  @Override
  public boolean matches(HttpServletRequest request) {

    if (request.getMethod() == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("null method in incoming request will not match this matcher.");
      }
      return false;
    }
    return supportedMethods.contains(request.getMethod());
  }

  @Override
  public String toString() {
    return "CustomHttpMethodMatcher [supportedMethods=" + supportedMethods + "]";
  }

}
