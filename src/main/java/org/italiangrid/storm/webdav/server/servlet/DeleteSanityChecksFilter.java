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
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TpcUtils;

public class DeleteSanityChecksFilter implements Filter, TpcUtils {

  final PathResolver resolver;

  public DeleteSanityChecksFilter(PathResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    if (WebDAVMethod.DELETE.name().equals(request.getMethod())
        && pathIsStorageAreaRoot(request, resolver)) {

      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("text/plain");
      response.getWriter().print("Cannot delete a storage area root");
      res.flushBuffer();
    } else {
      chain.doFilter(req, res);
    }
  }
}
