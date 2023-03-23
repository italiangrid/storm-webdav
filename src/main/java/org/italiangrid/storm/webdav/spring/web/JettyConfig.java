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
package org.italiangrid.storm.webdav.spring.web;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.italiangrid.storm.webdav.config.ConfigurationLogger;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.server.DefaultJettyServerCustomizer;
import org.italiangrid.storm.webdav.server.DefaultWebServerFactory;
import org.italiangrid.storm.webdav.server.util.JettyErrorPageHandler;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;

@Configuration
public class JettyConfig {

  @Bean
  JettyServerCustomizer defaultJettyServerCustomizer(ServiceConfigurationProperties serviceConfig,
      ServiceConfiguration configuration, StorageAreaConfiguration saConf,
      ServerProperties serverProperties, MetricRegistry registry, ConfigurationLogger confLogger,
      X509CertChainValidatorExt certChainValidator) {

    return new DefaultJettyServerCustomizer(serviceConfig, configuration, saConf, serverProperties,
        registry, confLogger, certChainValidator);

  }

  @Bean
  WebServerFactoryCustomizer<JettyServletWebServerFactory> defaultWebServerFactory(
      ServiceConfiguration configuration, ServerProperties serverProperties,
      JettyServerCustomizer serverCustomizer, MetricRegistry registry) {

    return new DefaultWebServerFactory(configuration, serverProperties, serverCustomizer, registry);
  }

  @Bean
  JettyServletWebServerFactory defaultJettyServletWebServerFactory() {
    return new JettyServletWebServerFactory() {
      @Override
      protected void postProcessWebAppContext(WebAppContext context) {
        context.setCompactPath(true);
      }

      @Override
      protected org.eclipse.jetty.webapp.Configuration[] getWebAppContextConfigurations(
          WebAppContext webAppContext, ServletContextInitializer... initializers) {

        List<org.eclipse.jetty.webapp.Configuration> configurations = newArrayList(
            Arrays.asList(super.getWebAppContextConfigurations(webAppContext, initializers)));

        configurations.add(getStormErrorPageConfiguration());
        return configurations.toArray(new org.eclipse.jetty.webapp.Configuration[0]);
      }

      private org.eclipse.jetty.webapp.Configuration getStormErrorPageConfiguration() {
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
                  handler.addErrorPage(ErrorPageErrorHandler.GLOBAL_ERROR_PAGE,
                      errorPage.getPath());
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
    };
  }
}
