/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
