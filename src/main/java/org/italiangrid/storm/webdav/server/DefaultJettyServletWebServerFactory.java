// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.jetty.ee11.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.ee11.webapp.AbstractConfiguration;
import org.eclipse.jetty.ee11.webapp.Configuration;
import org.eclipse.jetty.ee11.webapp.WebAppContext;
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;
import org.springframework.boot.web.error.ErrorPage;
import org.springframework.boot.web.server.servlet.ServletContextInitializers;

public class DefaultJettyServletWebServerFactory extends JettyServletWebServerFactory {

  @Override
  protected Configuration[] getWebAppContextConfigurations(
      WebAppContext webAppContext, ServletContextInitializers initializers) {

    List<Configuration> configurations =
        new ArrayList<>(
            Arrays.asList(super.getWebAppContextConfigurations(webAppContext, initializers)));

    configurations.add(getStormErrorPageConfiguration());
    return configurations.toArray(new Configuration[0]);
  }

  private Configuration getStormErrorPageConfiguration() {
    return new AbstractConfiguration(new AbstractConfiguration.Builder()) {

      @Override
      public void configure(WebAppContext context) throws Exception {
        ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
        context.setErrorHandler(errorHandler);
        addErrorPages(errorHandler, getErrorPages());
        errorHandler.setShowStacks(false);
      }

      private void addErrorPages(
          ErrorPageErrorHandler errorHandler, Collection<ErrorPage> errorPages) {
        for (ErrorPage errorPage : errorPages) {
          if (errorPage.isGlobal()) {
            errorHandler.addErrorPage(ErrorPageErrorHandler.GLOBAL_ERROR_PAGE, errorPage.getPath());
          } else {
            if (errorPage.getExceptionName() != null) {
              errorHandler.addErrorPage(errorPage.getExceptionName(), errorPage.getPath());
            } else {
              errorHandler.addErrorPage(errorPage.getStatusCode(), errorPage.getPath());
            }
          }
        }
      }
    };
  }
}
