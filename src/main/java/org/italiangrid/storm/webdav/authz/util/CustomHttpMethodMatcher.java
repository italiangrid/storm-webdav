// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
