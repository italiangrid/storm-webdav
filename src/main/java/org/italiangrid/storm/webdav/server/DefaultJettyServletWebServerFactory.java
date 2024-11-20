/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jetty.ee10.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.ee10.webapp.AbstractConfiguration;
import org.eclipse.jetty.ee10.webapp.Configuration;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.servlet.ServletContextInitializer;

public class DefaultJettyServletWebServerFactory extends JettyServletWebServerFactory {

  @Override
  protected Configuration[] getWebAppContextConfigurations(WebAppContext webAppContext,
      ServletContextInitializer... initializers) {

    List<Configuration> configurations = new ArrayList<>(
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

      private void addErrorPages(ErrorPageErrorHandler errorHandler,
          Collection<ErrorPage> errorPages) {
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
