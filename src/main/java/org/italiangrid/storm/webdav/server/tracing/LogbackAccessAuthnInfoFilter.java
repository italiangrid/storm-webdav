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
