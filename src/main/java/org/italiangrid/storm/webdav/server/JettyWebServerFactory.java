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
package org.italiangrid.storm.webdav.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.italiangrid.storm.webdav.config.ConfigurationLogger;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.italiangrid.storm.webdav.server.util.JettyErrorPageHandler;
import org.italiangrid.utils.jetty.TLSServerConnectorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;

import ch.qos.logback.access.jetty.RequestLogImpl;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;

@Component
public class JettyWebServerFactory extends JettyServletWebServerFactory
    implements JettyServerCustomizer {

  public static final String STORM_REQUEST_LOGGER_NAME =
      JettyWebServerFactory.class.getPackage().getName() + ".RequestLog";

  public static final Logger LOG = LoggerFactory.getLogger(JettyWebServerFactory.class);

  public static final String HTTP_CONNECTOR_NAME = "storm-http";
  public static final String HTTPS_CONNECTOR_NAME = "storm-https";

  final ServiceConfiguration configuration;
  final StorageAreaConfiguration saConf;

  final ServerProperties serverProperties;

  final MetricRegistry metricRegistry;

  final ConfigurationLogger confLogger;

  final X509CertChainValidatorExt certChainValidator;

  final ServiceConfigurationProperties serviceConfig;

  private void configureTLSConnector(Server server)
      throws KeyStoreException, CertificateException, IOException {

    TLSServerConnectorBuilder connectorBuilder =
        TLSServerConnectorBuilder.instance(server, certChainValidator);

    connectorBuilder.httpConfiguration().setSendServerVersion(false);
    connectorBuilder.httpConfiguration().setSendDateHeader(false);
    connectorBuilder.httpConfiguration()
      .setIdleTimeout(configuration.getConnectorMaxIdleTimeInMsec());

    connectorBuilder.httpConfiguration()
      .setOutputBufferSize(serviceConfig.getConnector().getOutputBufferSizeBytes());


    ServerConnector connector = connectorBuilder.withPort(configuration.getHTTPSPort())
      .withWantClientAuth(true)
      .withNeedClientAuth(configuration.requireClientCertificateAuthentication())
      .withCertificateFile(configuration.getCertificatePath())
      .withCertificateKeyFile(configuration.getPrivateKeyPath())
      .metricName("storm-https.connection")
      .metricRegistry(metricRegistry)
      .withConscrypt(configuration.useConscrypt())
      .withHttp2(configuration.enableHttp2())
      .withDisableJsseHostnameVerification(true)
      .withTlsProtocol(configuration.getTlsProtocol())
      .withAcceptors(serverProperties.getJetty().getThreads().getAcceptors())
      .withSelectors(serverProperties.getJetty().getThreads().getSelectors())
      .build();

    connector.setName(HTTPS_CONNECTOR_NAME);

    server.addConnector(connector);
    LOG.info("Configured TLS connector on port: {}. Conscrypt enabled: {}. HTTP/2 enabled: {}",
        configuration.getHTTPSPort(), configuration.useConscrypt(), configuration.enableHttp2());
  }

  private void configurePlainConnector(Server server) {

    HttpConfiguration plainConnectorConfig = new HttpConfiguration();
    plainConnectorConfig.setSendDateHeader(false);
    plainConnectorConfig.setSendServerVersion(false);

    plainConnectorConfig.setIdleTimeout(configuration.getConnectorMaxIdleTimeInMsec());

    InstrumentedConnectionFactory connFactory =
        new InstrumentedConnectionFactory(new HttpConnectionFactory(plainConnectorConfig),
            metricRegistry.timer("storm-http.connection"));

    ServerConnector connector = new ServerConnector(server, connFactory);

    connector.setName(HTTP_CONNECTOR_NAME);
    connector.setPort(configuration.getHTTPPort());

    server.addConnector(connector);
    LOG.info("Configured plain HTTP connector on port: {}", configuration.getHTTPPort());
  }

  private void configureRewriteHandler(Server server) throws MalformedURLException, IOException {

    RewriteHandler rh = new RewriteHandler();

    rh.setRewritePathInfo(true);
    rh.setRewriteRequestURI(true);

    RewriteRegexRule dropLegacyWebDAV = new RewriteRegexRule();
    dropLegacyWebDAV.setRegex("/webdav/(.*)");
    dropLegacyWebDAV.setReplacement("/$1");

    RewriteRegexRule dropLegacyFileTransfer = new RewriteRegexRule();
    dropLegacyFileTransfer.setRegex("/fileTransfer/(.*)");
    dropLegacyFileTransfer.setReplacement("/$1");

    rh.addRule(dropLegacyWebDAV);
    rh.addRule(dropLegacyFileTransfer);

    rh.setHandler(server.getHandler());

    InstrumentedHandler ih = new InstrumentedHandler(metricRegistry, "storm.http.handler");
    ih.setHandler(rh);
    server.setHandler(ih);

  }

  private ThreadPool configureThreadPool() {

    final int DEFAULT_IDLE_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(60);
    BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(configuration.getMaxQueueSize());
    final String PREFIX = "storm.http.thread-pool";

    InstrumentedQueuedThreadPool tp = new InstrumentedQueuedThreadPool(metricRegistry,
        serverProperties.getJetty().getThreads().getMax(),
        serverProperties.getJetty().getThreads().getMin(),
        DEFAULT_IDLE_TIMEOUT,
        queue,
        PREFIX);
    return tp;
  }

  public JettyWebServerFactory(ServiceConfigurationProperties serviceConfigurationProperties,
      StorageAreaConfiguration saConf, ServerProperties serverProperties, MetricRegistry registry,
      ConfigurationLogger confLogger, X509CertChainValidatorExt certChainValidator) {
    super(serviceConfigurationProperties.getHTTPPort());
    this.configuration = serviceConfigurationProperties;
    this.serviceConfig = serviceConfigurationProperties;
    this.saConf = saConf;
    this.serverProperties = serverProperties;
    this.certChainValidator = certChainValidator;
    this.metricRegistry = registry;
    this.confLogger = confLogger;
    this.addServerCustomizers(this);
    setRegisterDefaultServlet(false);
    setThreadPool(configureThreadPool());
  }

  private void configureAccessLog(Server server) {
    RequestLogImpl rli = new RequestLogImpl();

    rli.setQuiet(true);
    String accessLogConfiguration = configuration.getAccessLogConfigurationPath();

    if (isNullOrEmpty(accessLogConfiguration)) {
      LOG.info("Null or empty access log configuration... access log will go to standard output");
      rli.setResource("/logback-access.xml");
    } else {
      File accessLogConfFile = Paths.get(accessLogConfiguration).toFile();
      if (!accessLogConfFile.exists() || !accessLogConfFile.canRead()) {
        LOG.warn(
            "Access log configuration file '{}' does not exist or is not readable... disabling access log.",
            accessLogConfFile);
        return;
      } else {
        LOG.info("Jetty Access log configured from file '{}'.", accessLogConfFile);
        rli.setFileName(accessLogConfiguration);
      }
    }

    rli.start();
    server.setRequestLog(rli);
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

  @Override
  protected void postProcessWebAppContext(WebAppContext context) {
    context.setCompactPath(true);
  }

  @Override
  public void customize(Server server) {
    server.setConnectors(null);
    configurePlainConnector(server);
    try {
      configureTLSConnector(server);
      configureRewriteHandler(server);
      configureAccessLog(server);
    } catch (KeyStoreException | CertificateException | IOException e) {
      throw new StoRMWebDAVError(e);
    }

    server.setDumpAfterStart(false);
    server.setDumpBeforeStop(false);
    server.setStopAtShutdown(true);

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

    };
  }


  @PostConstruct
  protected void after() {
    confLogger.logConfiguration(LOG);
  }

}
