/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
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
package org.italiangrid.storm.webdav.server;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
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

  private final StorageAreaConfiguration saConfig;
  private final TemplateEngine engine;

  private final Map<String, String> saIndexMap;

  public SAIndexServlet(StorageAreaConfiguration config, TemplateEngine engine) {

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

    req.setAttribute(SA_INDEX_MAP_KEY, saIndexMap);

    resp.setHeader(CACHE_CONTROL, NO_CACHE);
    resp.setContentType(CONTENT_TYPE);

    WebContext ctxt = new WebContext(req, resp, getServletContext(),
      req.getLocale());

    engine.process(SA_INDEX_PAGE_NAME, ctxt, resp.getWriter());
  }

}
