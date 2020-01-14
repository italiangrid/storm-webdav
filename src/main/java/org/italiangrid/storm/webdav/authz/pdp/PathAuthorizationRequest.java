package org.italiangrid.storm.webdav.authz.pdp;

import javax.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;

public class PathAuthorizationRequest {

  final HttpServletRequest request;
  final Authentication authentication;
  
  @Nullable
  final String path;

  private PathAuthorizationRequest(HttpServletRequest request, Authentication authentication,
      String path) {
    this.request = request;
    this.authentication = authentication;
    this.path = path;
  }

  public static PathAuthorizationRequest newAuthorizationRequest(HttpServletRequest request,
      Authentication authentication, String path) {
    return new PathAuthorizationRequest(request, authentication, path);
  }
  
  public static PathAuthorizationRequest newAuthorizationRequest(HttpServletRequest request,
      Authentication authentication) {
    return new PathAuthorizationRequest(request, authentication, null);
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
  
}
