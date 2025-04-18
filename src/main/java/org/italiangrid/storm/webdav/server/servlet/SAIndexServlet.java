// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

import static org.italiangrid.storm.webdav.authn.AuthenticationUtils.getPalatableSubject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.web.PathConstants;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import io.opentelemetry.instrumentation.annotations.WithSpan;

public class SAIndexServlet extends HttpServlet {

  /** */
  private static final long serialVersionUID = -8193945050086639692L;

  private static final String CONTENT_TYPE = "text/html;charset=UTF-8";
  private static final String CACHE_CONTROL = "Cache-Control";
  private static final String NO_CACHE = "must-revalidate,no-cache,no-store";

  private static final String SA_INDEX_PAGE_NAME = "sa-index";
  private static final String SA_INDEX_MAP_KEY = "saIndexMap";

  public static final String AUTHN_KEY = "authn";
  public static final String AUTHN_SUBJECT_KEY = "authnSubject";

  public static final String STORM_HOSTNAME_KEY = "storm";
  public static final String OIDC_ENABLED_KEY = "oidcEnabled";

  private final transient OAuthProperties oauthProperties;
  private final transient StorageAreaConfiguration saConfig;
  private final transient ServiceConfigurationProperties serviceConfig;
  private final transient TemplateEngine engine;

  private final Map<String, String> saIndexMap;

  public SAIndexServlet(
      OAuthProperties oauthP,
      ServiceConfigurationProperties serviceConfig,
      StorageAreaConfiguration config,
      TemplateEngine engine) {

    this.oauthProperties = oauthP;
    this.serviceConfig = serviceConfig;
    this.saConfig = config;
    this.engine = engine;
    saIndexMap = new TreeMap<>();
    for (StorageAreaInfo sa : saConfig.getStorageAreaInfo()) {
      saIndexMap.put(sa.name(), sa.accessPoints().get(0));
    }
  }

  @WithSpan
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    SecurityContext securityContext = SecurityContextHolder.getContext();
    req.setAttribute(PathConstants.class.getSimpleName(), PathConstants.pathConstants());

    req.setAttribute(SA_INDEX_MAP_KEY, saIndexMap);

    req.setAttribute(AUTHN_KEY, securityContext.getAuthentication());

    req.setAttribute(AUTHN_SUBJECT_KEY, getPalatableSubject(securityContext.getAuthentication()));

    resp.setHeader(CACHE_CONTROL, NO_CACHE);
    resp.setContentType(CONTENT_TYPE);

    req.setAttribute(STORM_HOSTNAME_KEY, serviceConfig.getHostnames().get(0));
    req.setAttribute(OIDC_ENABLED_KEY, oauthProperties.isEnableOidc());

    final IWebExchange webExchange =
        JakartaServletWebApplication.buildApplication(this.getServletContext())
            .buildExchange(req, resp);

    WebContext ctxt = new WebContext(webExchange, req.getLocale());

    engine.process(SA_INDEX_PAGE_NAME, ctxt, resp.getWriter());
  }
}
