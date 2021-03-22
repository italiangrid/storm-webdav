/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.server.servlet;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    if (WebDAVMethod.MOVE.name().equals(req.getMethod()) && requestHasDestinationHeader(req)
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
