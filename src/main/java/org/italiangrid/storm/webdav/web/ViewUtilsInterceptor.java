// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.italiangrid.storm.webdav.authn.AuthenticationUtils;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.server.servlet.SAIndexServlet;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

public class ViewUtilsInterceptor implements HandlerInterceptor {

  final ServiceConfigurationProperties serviceConfig;
  final StorageAreaConfiguration saConfig;
  final OAuthProperties oauthProperties;

  public ViewUtilsInterceptor(
      ServiceConfigurationProperties properties,
      StorageAreaConfiguration saConfig,
      OAuthProperties oauthProperties) {

    this.serviceConfig = properties;
    this.saConfig = saConfig;
    this.oauthProperties = oauthProperties;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    SecurityContext context = SecurityContextHolder.getContext();
    request.setAttribute(SAIndexServlet.AUTHN_KEY, context.getAuthentication());
    request.setAttribute(
        SAIndexServlet.AUTHN_SUBJECT_KEY,
        AuthenticationUtils.getPalatableSubject(context.getAuthentication()));
    request.setAttribute(SAIndexServlet.STORM_HOSTNAME_KEY, serviceConfig.getHostnames().get(0));
    request.setAttribute(SAIndexServlet.OIDC_ENABLED_KEY, oauthProperties.isEnableOidc());
    return true;
  }
}
