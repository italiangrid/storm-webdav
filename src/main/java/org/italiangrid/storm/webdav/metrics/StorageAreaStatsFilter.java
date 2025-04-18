// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.MetricRegistry;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TpcUtils;

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

    if (sa != null) {
      String prefix = name(SA_KEY, sa.name(), REQUESTS_KEY);
      registry.meter(prefix).mark();
      if (response.getStatus() >= 400) {
        registry.meter(name(prefix, ERROR_KEY)).mark();

      } else {
        registry.meter(name(prefix, OK_KEY)).mark();
      }
    }
  }

  @WithSpan
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
