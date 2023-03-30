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

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.italiangrid.storm.webdav.server.util.JettyErrorPageHandler;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.servlet.ServletContextInitializer;

public class DefaultJettyServletWebServerFactory extends JettyServletWebServerFactory {

  @Override
  protected void postProcessWebAppContext(WebAppContext context) {
    context.setCompactPath(true);
  }

  @Override
  protected Configuration[] getWebAppContextConfigurations(WebAppContext webAppContext,
      ServletContextInitializer... initializers) {

    List<Configuration> configurations = newArrayList(
        Arrays.asList(super.getWebAppContextConfigurations(webAppContext, initializers)));

    configurations.add(getStormErrorPageConfiguration());
    return configurations.toArray(new Configuration[0]);
  }

  private Configuration getStormErrorPageConfiguration() {
    return new AbstractConfiguration() {

      @Override
      public void configure(WebAppContext context) throws Exception {
        JettyErrorPageHandler errorHandler = new JettyErrorPageHandler();
        context.setErrorHandler(errorHandler);
        addErrorPages(errorHandler, getErrorPages());
        errorHandler.setShowStacks(false);
      }

      private void addErrorPages(ErrorHandler errorHandler, Collection<ErrorPage> errorPages) {
        if (errorHandler instanceof ErrorPageErrorHandler) {
          ErrorPageErrorHandler handler = (ErrorPageErrorHandler) errorHandler;
          for (ErrorPage errorPage : errorPages) {
            if (errorPage.isGlobal()) {
              handler.addErrorPage(ErrorPageErrorHandler.GLOBAL_ERROR_PAGE, errorPage.getPath());
            } else {
              if (errorPage.getExceptionName() != null) {
                handler.addErrorPage(errorPage.getExceptionName(), errorPage.getPath());
              } else {
                handler.addErrorPage(errorPage.getStatusCode(), errorPage.getPath());
              }
            }
          }
        }
      }

    };
  }
}
