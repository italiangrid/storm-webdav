// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.tracing;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authn.PrincipalHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class LogbackAccessAuthnInfoFilter implements Filter {

  public static final String REMOTE_USER_ATTR_NAME = "storm.remoteUser";

  private final PrincipalHelper helper;

  public LogbackAccessAuthnInfoFilter(PrincipalHelper principalHelper) {
    this.helper = principalHelper;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    saveRemoteUserInRequest((HttpServletRequest) request);
    chain.doFilter(request, response);
  }

  protected void saveRemoteUserInRequest(HttpServletRequest request) {
    Authentication authn = SecurityContextHolder.getContext().getAuthentication();
    request.setAttribute(REMOTE_USER_ATTR_NAME, helper.getPrincipalAsString(authn));
  }
}
