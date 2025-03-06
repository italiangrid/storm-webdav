// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import org.italiangrid.storm.webdav.error.BadRequest;
import org.italiangrid.storm.webdav.error.ResourceNotFound;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class MoveRequestSanityChecksFilter implements Filter, TpcUtils {

  private final PathResolver resolver;

  @Autowired
  public MoveRequestSanityChecksFilter(PathResolver resolver) {
    this.resolver = resolver;
  }

  private void moveSanityChecks(HttpServletRequest req) throws MalformedURLException {
    if (WebDAVMethod.MOVE.name().equals(req.getMethod())
        && requestHasDestinationHeader(req)
        && !requestPathAndDestinationHeaderAreInSameStorageArea(req, resolver)) {
      throw new BadRequest("Move across storage areas is not supported");
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    try {
      moveSanityChecks(req);
    } catch (MalformedURLException | BadRequest | ResourceNotFound e) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.setContentType("text/plain");
      res.getWriter().print(e.getMessage());
      res.flushBuffer();
      return;
    }

    chain.doFilter(request, response);
  }
}
