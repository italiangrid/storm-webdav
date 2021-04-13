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
package org.italiangrid.storm.webdav.metrics;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Objects.isNull;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TpcUtils;

import com.codahale.metrics.MetricRegistry;

public class StorageAreaStatsFilter implements Filter, TpcUtils {

  public static final String SA_KEY = "sa";
  public static final String REQUESTS_KEY = "requests";
  public static final String OK_KEY = "ok-count";
  public static final String ERROR_KEY = "error-count";

  private final MetricRegistry registry;
  private final PathResolver resolver;

  public StorageAreaStatsFilter(MetricRegistry registry, PathResolver resolver) {
    this.registry = registry;
    this.resolver = resolver;
  }

  private void updateStats(HttpServletRequest request, HttpServletResponse response) {
    StorageAreaInfo sa = resolver.resolveStorageArea(getSerlvetRequestPath(request));

    if (!isNull(sa)) {
      String prefix = name(SA_KEY, sa.name(), REQUESTS_KEY);
      registry.meter(prefix).mark();
      if (response.getStatus() >= 400) {
        registry.meter(name(prefix, ERROR_KEY)).mark();

      } else {
        registry.meter(name(prefix, OK_KEY)).mark();
      }
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    try {
      chain.doFilter(request, response);
    } finally {
      updateStats((HttpServletRequest) request, (HttpServletResponse) response);
    }
  }

}
