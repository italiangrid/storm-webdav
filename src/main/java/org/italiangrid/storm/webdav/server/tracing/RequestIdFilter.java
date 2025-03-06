// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.tracing;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import org.slf4j.MDC;

public class RequestIdFilter implements Filter {

  public static final String REQUEST_ID_ATTRIBUTE_NAME = "storm.requestId";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    RequestIdHolder.setRandomId();
    request.setAttribute(REQUEST_ID_ATTRIBUTE_NAME, RequestIdHolder.getRequestId());
    MDC.put(REQUEST_ID_ATTRIBUTE_NAME, RequestIdHolder.getRequestId());
    try {
      chain.doFilter(request, response);
    } finally {
      RequestIdHolder.cleanup();
      MDC.clear();
    }
  }
}
