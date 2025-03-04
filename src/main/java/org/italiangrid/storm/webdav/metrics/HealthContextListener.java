// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.metrics.servlets.HealthCheckServlet.ContextListener;

@Component
public class HealthContextListener extends ContextListener {

  private HealthCheckRegistry registry;

  @Autowired
  public HealthContextListener(HealthCheckRegistry r) {
    this.registry = r;

  }

  @Override
  protected HealthCheckRegistry getHealthCheckRegistry() {
    return registry;
  }

}
