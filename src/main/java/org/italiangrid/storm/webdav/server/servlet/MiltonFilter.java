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
package org.italiangrid.storm.webdav.server.servlet;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.milton.StoRMHTTPManagerBuilder;
import org.italiangrid.storm.webdav.milton.StoRMMiltonRequest;
import org.italiangrid.storm.webdav.milton.StoRMResourceFactory;
import org.italiangrid.storm.webdav.milton.util.ReplaceContentStrategy;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.servlet.MiltonServlet;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MiltonFilter implements Filter {

  public static final Logger LOG = LoggerFactory.getLogger(MiltonFilter.class);

  static final Set<String> WEBDAV_METHOD_SET = new HashSet<String>();
  static final String SA_ROOT_PATH = "sa-root";

  static {
    for (WebDAVMethod m : WebDAVMethod.values()) {
      WEBDAV_METHOD_SET.add(m.name());
    }
  }

  private HttpManager miltonHTTPManager;

  private ServletContext servletContext;

  private final FilesystemAccess filesystemAccess;

  private final ExtendedAttributesHelper attrsHelper;

  private final PathResolver resolver;

  private final ReplaceContentStrategy rcs;

  public MiltonFilter(FilesystemAccess fsAccess, ExtendedAttributesHelper attrsHelper,
      PathResolver resolver, ReplaceContentStrategy rcs) {

    this.filesystemAccess = fsAccess;
    this.attrsHelper = attrsHelper;
    this.resolver = resolver;
    this.rcs = rcs;
  }

  private void initMiltonHTTPManager(ServletContext context) {

    final StoRMHTTPManagerBuilder builder = new StoRMHTTPManagerBuilder();

    final StoRMResourceFactory resourceFactory =
        new StoRMResourceFactory(filesystemAccess, attrsHelper, resolver, rcs);

    builder.setResourceFactory(resourceFactory);

    miltonHTTPManager = builder.buildHttpManager();

  }

  @Override
  public void init(FilterConfig config) throws ServletException {

    servletContext = config.getServletContext();

    if (isNull(miltonHTTPManager)) {
      initMiltonHTTPManager(servletContext);
    }

  }

  private boolean isWebDAVMethod(ServletRequest request) {

    return WEBDAV_METHOD_SET.contains(((HttpServletRequest) request).getMethod());
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    if (isWebDAVMethod(request)) {
      doMilton((HttpServletRequest) request, (HttpServletResponse) response);
    } else
      chain.doFilter(request, response);

  }

  public void doMilton(HttpServletRequest request, HttpServletResponse response) {

    LOG.trace("doMilton: req: {}, res: {}", request, response);

    try {
      // Is this really needed?
      MiltonServlet.setThreadlocals((HttpServletRequest) request, (HttpServletResponse) response);

      Request miltonReq = new StoRMMiltonRequest(request, servletContext);

      Response miltonRes = new io.milton.servlet.ServletResponse(response);
      miltonHTTPManager.process(miltonReq, miltonRes);

    } finally {

      MiltonServlet.clearThreadlocals();

      try {

        response.getOutputStream().flush();
        response.flushBuffer();

      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
        throw new RuntimeException(e.getMessage(), e);
      }

    }

  }

  public void setMiltonHTTPManager(HttpManager miltonHTTPManager) {
    this.miltonHTTPManager = miltonHTTPManager;
  }

  @Override
  public void destroy() {

  }

}
