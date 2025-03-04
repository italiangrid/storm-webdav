// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.util;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

public class EmptyPathRequestMatcher implements RequestMatcher {

  @Override
  public boolean matches(HttpServletRequest request) {
    return !StringUtils.hasLength(getRequestPath(request));
  }

  private String getRequestPath(HttpServletRequest request) {

    String url = request.getServletPath();

    if (request.getPathInfo() != null) {
      url += request.getPathInfo();
    }

    return url;
  }

  @Override
  public String toString() {
    return "EmptyPathRequestMatcher";
  }

}
