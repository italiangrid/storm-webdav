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
package org.italiangrid.storm.webdav.spring.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;

public class HttpMethodRequestRejectedHandler implements RequestRejectedHandler {

  private static final Log logger = LogFactory.getLog(HttpMethodRequestRejectedHandler.class);

  private final List<String> allowedMethods;

  public HttpMethodRequestRejectedHandler(List<String> allowedMethods) {

    this.allowedMethods = new ArrayList<>();
    this.allowedMethods.addAll(allowedMethods);
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      RequestRejectedException requestRejectedException) throws IOException, ServletException {

    logger.debug(
        LogMessage.format("Rejecting request due to: %s", requestRejectedException.getMessage()),
        requestRejectedException);

    if (!allowedMethods.contains(request.getMethod())) {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    } else {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

}
