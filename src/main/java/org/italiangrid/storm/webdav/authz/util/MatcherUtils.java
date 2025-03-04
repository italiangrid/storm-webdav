// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.util;

import jakarta.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest;

public interface MatcherUtils {

  public default String getRequestPath(HttpServletRequest request) {
    String url = request.getServletPath();

    if (request.getPathInfo() != null) {
      url += request.getPathInfo();
    }

    return url;
  }

  public default String requestToString(PathAuthorizationRequest request) {

    final String requestString = String.format("%s %s", request.getRequest().getMethod(),
        getRequestPath(request.getRequest()));

    if (request.getPath() == null) {
      return requestString;
    } else {
      return String.format("%s (%s %s)", requestString, request.getMethod(), request.getPath());
    }

  }

}
