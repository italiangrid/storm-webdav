/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.authz.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
