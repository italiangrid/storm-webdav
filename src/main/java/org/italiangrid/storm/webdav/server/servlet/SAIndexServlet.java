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

import static org.italiangrid.storm.webdav.authn.AuthenticationUtils.getPalatableSubject;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

public class SAIndexServlet extends HttpServlet {

  /**
   * 
   */
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

  private final OAuthProperties oauthProperties;
  private final StorageAreaConfiguration saConfig;
  private final ServiceConfigurationProperties serviceConfig;
  private final TemplateEngine engine;

  private final Map<String, String> saIndexMap;

  public SAIndexServlet(OAuthProperties oauthP, ServiceConfigurationProperties serviceConfig,
      StorageAreaConfiguration config, TemplateEngine engine) {

    this.oauthProperties = oauthP;
    this.serviceConfig = serviceConfig;
    this.saConfig = config;
    this.engine = engine;
    saIndexMap = new TreeMap<String, String>();
    for (StorageAreaInfo sa : saConfig.getStorageAreaInfo()) {
      saIndexMap.put(sa.name(), sa.accessPoints().get(0));
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    SecurityContext securityContext = SecurityContextHolder.getContext();
    req.setAttribute(SA_INDEX_MAP_KEY, saIndexMap);

    req.setAttribute(AUTHN_KEY, securityContext.getAuthentication());

    req.setAttribute(AUTHN_SUBJECT_KEY, getPalatableSubject(securityContext.getAuthentication()));

    resp.setHeader(CACHE_CONTROL, NO_CACHE);
    resp.setContentType(CONTENT_TYPE);

    req.setAttribute(STORM_HOSTNAME_KEY, serviceConfig.getHostnames().get(0));
    req.setAttribute(OIDC_ENABLED_KEY, oauthProperties.isEnableOidc());

    WebContext ctxt = new WebContext(req, resp, getServletContext(), req.getLocale());

    engine.process(SA_INDEX_PAGE_NAME, ctxt, resp.getWriter());
  }

}
