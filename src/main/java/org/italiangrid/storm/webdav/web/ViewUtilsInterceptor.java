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

  public ViewUtilsInterceptor(ServiceConfigurationProperties properties,
      StorageAreaConfiguration saConfig, OAuthProperties oauthProperties) {

    this.serviceConfig = properties;
    this.saConfig = saConfig;
    this.oauthProperties = oauthProperties;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    SecurityContext context = SecurityContextHolder.getContext();
    request.setAttribute(SAIndexServlet.AUTHN_KEY, context.getAuthentication());
    request.setAttribute(SAIndexServlet.AUTHN_SUBJECT_KEY,
        AuthenticationUtils.getPalatableSubject(context.getAuthentication()));
    request.setAttribute(SAIndexServlet.STORM_HOSTNAME_KEY, serviceConfig.getHostnames().get(0));
    request.setAttribute(SAIndexServlet.OIDC_ENABLED_KEY, oauthProperties.isEnableOidc());
    return true;
  }

}
