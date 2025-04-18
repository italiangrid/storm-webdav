// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.eclipse.jetty.ee10.servlet.ResourceServlet;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.content.HttpContent;
import org.eclipse.jetty.util.URIUtil;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.scitag.SciTagTransfer;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.server.servlet.resource.StoRMResourceHttpContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class StoRMServlet extends ResourceServlet {
  public static final Logger LOG = LoggerFactory.getLogger(StoRMServlet.class);

  /** */
  private static final long serialVersionUID = 4204673943980786498L;

  final transient PathResolver pathResolver;
  final transient TemplateEngine templateEngine;
  final transient ServiceConfigurationProperties serviceConfig;
  final transient OAuthProperties oauthProperties;

  public StoRMServlet(OAuthProperties oauthP, ServiceConfigurationProperties serviceConfig,
      PathResolver resolver, TemplateEngine engine) {
    super();
    oauthProperties = oauthP;
    pathResolver = resolver;
    templateEngine = engine;
    this.serviceConfig = serviceConfig;
  }

  @Override
  public void init() throws ServletException {
    super.init();
    this.getResourceService()
      .setHttpContentFactory(new StoRMResourceHttpContentFactory(null, MimeTypes.DEFAULTS,
          oauthProperties, serviceConfig, pathResolver, templateEngine));
  }

  // Similar to getInitBoolean of
  // https://github.com/jetty/jetty.project/blob/jetty-12.0.x/jetty-ee10/jetty-ee10-servlet/src/main/java/org/eclipse/jetty/ee10/servlet/ResourceServlet.java
  private boolean getInitBooleanStoRM(String name, boolean defaultValue) {
    String value = getInitParameter(name);
    if (value == null || value.isEmpty())
      return defaultValue;
    return (value.startsWith("t") || value.startsWith("T") || value.startsWith("y")
        || value.startsWith("Y") || value.startsWith("1"));
  }

  @WithSpan
  @Override
  protected String getEncodedPathInContext(HttpServletRequest request, boolean included) {
    String servletPath = null;
    String pathInfo = null;

    if (included) {
      servletPath = getInitBooleanStoRM("pathInfoOnly", false) ? "/"
          : (String) request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
      pathInfo = (String) request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
      if (servletPath == null) {
        servletPath = request.getServletPath();
        pathInfo = request.getPathInfo();
      }
    } else {
      servletPath = getInitBooleanStoRM("pathInfoOnly", false) ? "/" : request.getServletPath();
      pathInfo = request.getPathInfo();
    }

    return URIUtil.addPaths(servletPath, pathInfo);
  }

  @WithSpan
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      SciTag scitag = (SciTag) request.getAttribute(SciTag.SCITAG_ATTRIBUTE);
      SciTagTransfer scitagTransfer = null;
      if (scitag != null) {
        scitagTransfer = new SciTagTransfer(scitag, request.getLocalAddr(), request.getLocalPort(),
            request.getRemoteAddr(), request.getRemotePort());
        scitagTransfer.writeStart();
      }
      super.doGet(request, response);
      if (scitagTransfer != null) {
        scitagTransfer.writeEnd();
      }

  }

  @WithSpan
  @Override
  protected void doHead(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    final boolean included = request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null;
    final String pathInContext = getEncodedPathInContext(request, included);

    final HttpContent content =
        getResourceService().getHttpContentFactory().getContent(pathInContext);

    if (content == null || !content.getResource().exists()) {

      if (included) {
        throw new FileNotFoundException("!" + pathInContext);
      }

      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else {
      response.setHeader(HttpHeader.LAST_MODIFIED.asString(), content.getLastModified().getValue());
      if (content.getContentLength() != null) {
        response.setHeader(HttpHeader.CONTENT_LENGTH.asString(),
            content.getContentLength().getValue());
      }
      if (content.getContentType() != null) {
        response.setHeader(HttpHeader.CONTENT_TYPE.asString(), content.getContentType().getValue());
      }
      response.setHeader(HttpHeader.ACCEPT_RANGES.asString(), "bytes");
    }
  }

  @WithSpan
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  @WithSpan
  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setHeader(HttpHeader.ALLOW.asString(), "GET,HEAD,OPTIONS");
  }
}
