package org.italiangrid.storm.webdav.server.tracing;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

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
