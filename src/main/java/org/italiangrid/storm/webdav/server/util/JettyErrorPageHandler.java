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
package org.italiangrid.storm.webdav.server.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.ee10.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.server.Request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JettyErrorPageHandler extends ErrorPageErrorHandler {

  private static final Set<String> HANDLED_HTTP_METHODS =
      new HashSet<>(Arrays.asList("GET", "POST", "HEAD"));

  @Override
  public boolean errorPageForMethod(String method) {
    return true;
  }

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    if (!HANDLED_HTTP_METHODS.contains(baseRequest.getMethod())) {
      baseRequest.setMethod("GET");
    }
    super.doError(target, baseRequest, request, response);
  }

}
