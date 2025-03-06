// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.spring.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      RequestRejectedException requestRejectedException)
      throws IOException, ServletException {

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
