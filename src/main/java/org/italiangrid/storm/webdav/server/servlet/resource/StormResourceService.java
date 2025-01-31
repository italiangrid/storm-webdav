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
package org.italiangrid.storm.webdav.server.servlet.resource;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.server.ResourceService;
import org.eclipse.jetty.util.URIUtil;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.scitag.SciTagTransfer;

public class StormResourceService extends ResourceService {

  private String pathInContext(HttpServletRequest request) {
    String servletPath = null;
    String pathInfo = null;

    boolean included = request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null;

    if (included) {
      servletPath = isPathInfoOnly() ? "/"
          : (String) request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
      pathInfo = (String) request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
      if (servletPath == null) {
        servletPath = request.getServletPath();
        pathInfo = request.getPathInfo();
      }
    } else {
      servletPath = isPathInfoOnly() ? "/" : request.getServletPath();
      pathInfo = request.getPathInfo();
    }

    return URIUtil.addPaths(servletPath, pathInfo);
  }

  @Override
  public boolean doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    SciTag scitag = (SciTag) request.getAttribute(SciTag.SCITAG_ATTRIBUTE);
    SciTagTransfer scitagTransfer = null;
    if (scitag != null) {
      scitagTransfer = new SciTagTransfer(scitag, request.getLocalAddr(), request.getLocalPort(),
          request.getRemoteAddr(), request.getRemotePort());
      scitagTransfer.writeStart();
    }
    boolean result = super.doGet(request, response);
    if (scitagTransfer != null) {
      scitagTransfer.writeEnd();
    }
    return result;
  }

  public boolean doHead(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    final boolean included = request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null;
    final String pathInContext = pathInContext(request);

    final HttpContent content =
        getContentFactory().getContent(pathInContext, response.getBufferSize());

    if (content == null || !content.getResource().exists()) {

      if (included) {
        throw new FileNotFoundException("!" + pathInContext);
      }

      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return response.isCommitted();
    }

    putHeaders(response, content, content.getContentLengthValue());
    return true;
  }

}
