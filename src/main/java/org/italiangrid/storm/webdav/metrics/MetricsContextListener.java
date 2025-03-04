// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.metrics.servlets.MetricsServlet.ContextListener;

@Component
public class MetricsContextListener extends ContextListener {

  private MetricRegistry registry;


  @Autowired
  public MetricsContextListener(MetricRegistry r) {

    registry = r;
  }

  @Override
  protected MetricRegistry getMetricRegistry() {
    return registry;
  }

}
