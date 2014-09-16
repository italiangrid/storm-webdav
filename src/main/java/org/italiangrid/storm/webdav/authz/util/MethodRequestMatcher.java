/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
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

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class MethodRequestMatcher implements RequestMatcher {

  private static final Logger logger = LoggerFactory
    .getLogger(MethodRequestMatcher.class);

  private final String[] methods;

  public MethodRequestMatcher(String... methods) {

    this.methods = methods;

  }

  @Override
  public boolean matches(HttpServletRequest request) {

    for (String m : methods) {

      if (request.getMethod().matches(m)) {
        return true;
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Request not matched by this matcher: method {} not in {}.",
        request.getMethod(), methods);
    }
    return false;
  }
  
  @Override
  public String toString() {
    return String.format("MethodRequestMatcher [method in '%s']", Arrays.toString(methods)); 
  }

}
