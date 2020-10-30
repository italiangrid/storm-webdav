package org.italiangrid.storm.webdav.server.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;

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
