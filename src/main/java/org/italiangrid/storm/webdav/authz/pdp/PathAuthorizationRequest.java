// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.pdp;

import jakarta.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.server.servlet.WebDAVMethod;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;

public class PathAuthorizationRequest {

  final HttpServletRequest request;
  final Authentication authentication;

  @Nullable
  final String path;

  final String method;

  private PathAuthorizationRequest(HttpServletRequest request, Authentication authentication,
      String path, String method) {
    this.request = request;
    this.authentication = authentication;
    this.path = path;
    this.method = method;
  }

  public static PathAuthorizationRequest newAuthorizationRequest(HttpServletRequest request,
      Authentication authentication, String path, WebDAVMethod method) {
    return new PathAuthorizationRequest(request, authentication, path, method.name());
  }

  public static PathAuthorizationRequest newAuthorizationRequest(HttpServletRequest request,
      Authentication authentication, String path) {
    return new PathAuthorizationRequest(request, authentication, path, null);
  }

  public static PathAuthorizationRequest newAuthorizationRequest(HttpServletRequest request,
      Authentication authentication) {
    return new PathAuthorizationRequest(request, authentication, null, null);
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public String getPath() {
    return path;
  }

  public String getMethod() {
    return method;
  }
}
