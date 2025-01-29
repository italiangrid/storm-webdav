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
