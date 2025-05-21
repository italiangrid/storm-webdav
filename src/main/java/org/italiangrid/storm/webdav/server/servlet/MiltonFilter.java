// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Request.Method;
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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.milton.StoRMHTTPManagerBuilder;
import org.italiangrid.storm.webdav.milton.StoRMMiltonRequest;
import org.italiangrid.storm.webdav.milton.StoRMResourceFactory;
import org.italiangrid.storm.webdav.milton.util.ReplaceContentStrategy;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.scitag.SciTagTransfer;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiltonFilter implements Filter {

  public static final Logger LOG = LoggerFactory.getLogger(MiltonFilter.class);

  static final Set<String> WEBDAV_METHOD_SET = new HashSet<>();
  static final String SA_ROOT_PATH = "sa-root";

  static {
    for (WebDAVMethod m : WebDAVMethod.values()) {
      WEBDAV_METHOD_SET.add(m.name());
    }
  }

  // List of acceptable WebDAV methods on stub files
  static final Set<String> WEBDAV_METHOD_ON_STUB_SET =
      Set.of(WebDAVMethod.OPTIONS.name(), WebDAVMethod.DELETE.name(), WebDAVMethod.PROPFIND.name());

  private HttpManager miltonHTTPManager;

  private ServletContext servletContext;

  private final FilesystemAccess filesystemAccess;

  private final ExtendedAttributesHelper attrsHelper;

  private final PathResolver resolver;

  private final ReplaceContentStrategy rcs;

  public MiltonFilter(
      FilesystemAccess fsAccess,
      ExtendedAttributesHelper attrsHelper,
      PathResolver resolver,
      ReplaceContentStrategy rcs) {

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

    if (miltonHTTPManager == null) {
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
    } else {
      chain.doFilter(request, response);
    }
  }

  public void doMilton(HttpServletRequest request, HttpServletResponse response) {

    LOG.trace("doMilton: req: {}, res: {}", request, response);

    try {
      // Is this really needed?
      MiltonServlet.setThreadlocals(request, response);

      Request miltonReq = new StoRMMiltonRequest(request, servletContext);

      Response miltonRes = new io.milton.servlet.ServletResponse(response);
      if (resolver.resolveStorageArea(miltonReq.getAbsolutePath()).tapeEnabled()
          && resolver.isStub(miltonReq.getAbsolutePath())
          && !WEBDAV_METHOD_ON_STUB_SET.contains(request.getMethod())) {
        miltonRes.sendError(
            Response.Status.SC_UNSUPPORTED_MEDIA_TYPE, "Called a WebDAV method on a stub file");
        return;
      }
      SciTag scitag = (SciTag) request.getAttribute(SciTag.SCITAG_ATTRIBUTE);
      if (scitag != null) {
        SciTagTransfer scitagTransfer =
            new SciTagTransfer(
                scitag,
                request.getLocalAddr(),
                request.getLocalPort(),
                request.getRemoteAddr(),
                request.getRemotePort());
        scitagTransfer.writeStart();
        request.setAttribute(SciTagTransfer.SCITAG_TRANSFER_ATTRIBUTE, scitagTransfer);
      }
      miltonHTTPManager.process(miltonReq, miltonRes);
      if (miltonReq.getMethod() == Method.PUT
          && resolver.resolveStorageArea(miltonReq.getAbsolutePath()).tapeEnabled()) {
        try {
          attrsHelper.setPremigrateAttribute(resolver.getPath(miltonReq.getAbsolutePath()));
        } catch (IOException e) {
          LOG.warn(
              "Error setting premigrate extended attribute to {}: {}",
              resolver.resolvePath(miltonReq.getAbsolutePath()),
              e.getMessage(),
              e);
        }
      }

    } finally {

      MiltonServlet.clearThreadlocals();
      SciTagTransfer scitagTransfer =
          (SciTagTransfer) request.getAttribute(SciTagTransfer.SCITAG_TRANSFER_ATTRIBUTE);
      if (scitagTransfer != null) {
        scitagTransfer.writeEnd();
      }

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
  public void destroy() {}
}
