package org.italiangrid.storm.webdav.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet.ContextListener;

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
