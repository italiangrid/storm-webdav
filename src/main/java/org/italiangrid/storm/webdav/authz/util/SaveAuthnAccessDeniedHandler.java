// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.util;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.authn.PrincipalHelper;
import org.italiangrid.storm.webdav.server.tracing.LogbackAccessAuthnInfoFilter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

public class SaveAuthnAccessDeniedHandler implements AccessDeniedHandler {

  private final PrincipalHelper helper;
  private final AccessDeniedHandler delegate;

  public SaveAuthnAccessDeniedHandler(PrincipalHelper principalHelper,
      AccessDeniedHandler delegate) {
    this.delegate = delegate;
    this.helper = principalHelper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {

    Authentication authn = SecurityContextHolder.getContext().getAuthentication();
    request.setAttribute(LogbackAccessAuthnInfoFilter.REMOTE_USER_ATTR_NAME,
        helper.getPrincipalAsString(authn));
    delegate.handle(request, response, accessDeniedException);

  }

}
