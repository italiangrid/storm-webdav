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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.Resource;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.server.servlet.resource.StormResourceService;
import org.italiangrid.storm.webdav.server.servlet.resource.StormResourceWrapper;
import org.thymeleaf.TemplateEngine;

public class StoRMServlet extends DefaultServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 4204673943980786498L;

  final PathResolver pathResolver;
  final TemplateEngine templateEngine;
  final ServiceConfigurationProperties serviceConfig;
  final StormResourceService resourceService;

  public StoRMServlet(ServiceConfigurationProperties serviceConfig, PathResolver resolver,
      TemplateEngine engine, StormResourceService rs) {
    super(rs);
    resourceService = rs;
    pathResolver = resolver;
    templateEngine = engine;
    this.serviceConfig = serviceConfig;
  }

  @Override
  public Resource getResource(String pathInContext) {

    String resolvedPath = pathResolver.resolvePath(pathInContext);

    if (resolvedPath == null) {
      return null;
    }

    File f = new File(resolvedPath);

    if (!f.exists()) {
      return null;
    }

    return new StormResourceWrapper(serviceConfig, templateEngine, Resource.newResource(f));

  }

  @Override
  protected void doHead(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    resourceService.doHead(request, response);
  }


  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setHeader("Allow", "GET,HEAD,OPTIONS");
  }
}
