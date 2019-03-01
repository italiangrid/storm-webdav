/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

public class HeaderRegexMatcher implements RequestMatcher {

  private static final Logger logger = LoggerFactory
    .getLogger(HeaderRegexMatcher.class);

  private final String headerName;
  private final String pattern;

  private final AntPathMatcher matcher;

  public HeaderRegexMatcher(String headerName, String expression) {

    this.headerName = headerName;
    this.pattern = expression;
    this.matcher = new AntPathMatcher();
  }

  @Override
  public boolean matches(HttpServletRequest request) {

    String headerValue = request.getHeader(headerName);
    if (headerValue != null) {

      boolean matches = matcher.match(pattern, headerValue);

      if (logger.isDebugEnabled()) {

        logger.debug("Request header value {} {} match with pattern {}",
          headerValue, (matches ? "does" : "does not"), pattern);
      }
      return matches;
    }
    return false;
  }

  @Override
  public String toString() {
    
    return String.format("HeaderRequestMatcher [header name=%s, pattern=%s]", headerName, pattern);
  }
}
