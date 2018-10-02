/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;

import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.italiangrid.storm.webdav.config.ConfigurationLogger;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.italiangrid.utils.jetty.TLSServerConnectorBuilder;
import org.italiangrid.utils.jetty.ThreadPoolBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.stereotype.Component;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.codahale.metrics.jetty9.InstrumentedHandler;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;

@Component
public class JettyWebServerFactory extends JettyServletWebServerFactory
    implements JettyServerCustomizer {

  public static final Logger LOG = LoggerFactory.getLogger(JettyWebServerFactory.class);

  public static final String HTTP_CONNECTOR_NAME = "storm-http";
  public static final String HTTPS_CONNECTOR_NAME = "storm-https";

  final ServiceConfiguration configuration;
  final StorageAreaConfiguration saConf;

  @Autowired
  MetricRegistry metricRegistry;

  @Autowired
  ConfigurationLogger confLogger;

  @Autowired
  X509CertChainValidatorExt certChainValidator;

  private void configureTLSConnector(Server server)
      throws KeyStoreException, CertificateException, IOException {

    TLSServerConnectorBuilder connectorBuilder =
        TLSServerConnectorBuilder.instance(server, certChainValidator);

    connectorBuilder.httpConfiguration().setSendServerVersion(false);
    connectorBuilder.httpConfiguration().setSendDateHeader(false);

    ServerConnector connector = connectorBuilder.withPort(configuration.getHTTPSPort())
      .withWantClientAuth(true)
      .withNeedClientAuth(configuration.requireClientCertificateAuthentication())
      .withCertificateFile(configuration.getCertificatePath())
      .withCertificateKeyFile(configuration.getPrivateKeyPath())
      .metricName("storm-https.connection")
      .metricRegistry(metricRegistry)
      .build();

    connector.setName(HTTPS_CONNECTOR_NAME);
    server.addConnector(connector);
    LOG.info("Configured TLS connector on port: {}", configuration.getHTTPSPort());
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

  private void configureRewritedHandler(Server server) throws MalformedURLException, IOException {

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
    return ThreadPoolBuilder.instance()
      .withMaxRequestQueueSize(configuration.getMaxQueueSize())
      .withMaxThreads(configuration.getMaxConnections())
      .withMinThreads(5)
      .registry(metricRegistry)
      .build();
  }

  @Autowired
  public JettyWebServerFactory(ServiceConfiguration serviceConfiguration,
      StorageAreaConfiguration saConf) {
    super(serviceConfiguration.getHTTPPort());
    this.configuration = serviceConfiguration;
    this.saConf = saConf;
    this.addServerCustomizers(this);
    setRegisterDefaultServlet(false);
    setThreadPool(configureThreadPool());
  }

  @Override
  protected void postProcessWebAppContext(WebAppContext context) {
    context.setCompactPath(true);

    ErrorHandler eh = new ErrorHandler();
    eh.setShowStacks(false);
    context.setErrorHandler(eh);

  }

  @Override
  public void customize(Server server) {
    server.setConnectors(null);
    configurePlainConnector(server);
    try {
      configureTLSConnector(server);
      configureRewritedHandler(server);
    } catch (KeyStoreException | CertificateException | IOException e) {
      throw new StoRMWebDAVError(e);
    }


    server.setDumpAfterStart(false);
    server.setDumpBeforeStop(false);
    server.setStopAtShutdown(true);
  }

  @PostConstruct
  protected void after() {
    confLogger.logConfiguration(LOG);
  }

}
