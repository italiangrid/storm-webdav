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

import static com.google.common.base.Strings.isNullOrEmpty;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

public class EmptyPathRequestMatcher implements RequestMatcher {

  @Override
  public boolean matches(HttpServletRequest request) {
    return isNullOrEmpty(getRequestPath(request));
  }

  private String getRequestPath(HttpServletRequest request) {

    String url = request.getServletPath();

    if (request.getPathInfo() != null) {
      url += request.getPathInfo();
    }

    return url;
  }

  @Override
  public String toString() {
    return "EmptyPathRequestMatcher";
  }

}
