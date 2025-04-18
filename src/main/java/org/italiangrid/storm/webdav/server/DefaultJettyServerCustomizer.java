// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server;

import java.io.File;
import java.nio.file.Paths;

import org.eclipse.jetty.rewrite.handler.CompactPathRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.italiangrid.storm.webdav.config.ConfigurationLogger;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.util.StringUtils;

import com.codahale.metrics.MetricRegistry;

import ch.qos.logback.access.jetty.RequestLogImpl;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import io.dropwizard.metrics.jetty12.InstrumentedConnectionFactory;
import io.dropwizard.metrics.jetty12.ee10.InstrumentedEE10Handler;
import jakarta.annotation.PostConstruct;

public class DefaultJettyServerCustomizer implements JettyServerCustomizer {

  public static final Logger LOG = LoggerFactory.getLogger(DefaultJettyServerCustomizer.class);

  public static final String HTTP_CONNECTOR_NAME = "storm-http";
  public static final String HTTPS_CONNECTOR_NAME = "storm-https";

  final ServiceConfiguration configuration;
  final StorageAreaConfiguration saConf;
  final ServerProperties serverProperties;
  final MetricRegistry metricRegistry;
  final ConfigurationLogger confLogger;
  final X509CertChainValidatorExt certChainValidator;
  final ServiceConfigurationProperties serviceConfig;

  public DefaultJettyServerCustomizer(ServiceConfigurationProperties serviceConfig,
      ServiceConfiguration configuration, StorageAreaConfiguration saConf,
      ServerProperties serverProperties, MetricRegistry registry, ConfigurationLogger confLogger,
      X509CertChainValidatorExt certChainValidator) {

    this.configuration = configuration;
    this.serviceConfig = serviceConfig;
    this.saConf = saConf;
    this.serverProperties = serverProperties;
    this.certChainValidator = certChainValidator;
    this.metricRegistry = registry;
    this.confLogger = confLogger;
  }

  @Override
  public void customize(Server server) {

    server.setConnectors(null);
    configurePlainConnector(server);
    configureTLSConnector(server);
    configureRewriteHandler(server);
    configureAccessLog(server);

    server.setDumpAfterStart(false);
    server.setDumpBeforeStop(false);
    server.setStopAtShutdown(true);
  }

  private void configurePlainConnector(Server server) {

    HttpConfiguration plainConnectorConfig = new HttpConfiguration();
    plainConnectorConfig.setSendDateHeader(false);
    plainConnectorConfig.setSendServerVersion(false);

    plainConnectorConfig.setIdleTimeout(configuration.getConnectorMaxIdleTimeInMsec());

    InstrumentedConnectionFactory connFactory =
        new InstrumentedConnectionFactory(new HttpConnectionFactory(plainConnectorConfig),
            metricRegistry.timer("storm-http.connection"));

    NetworkTrafficServerConnector connector =
        new NetworkTrafficServerConnector(server, connFactory);

    connector.setName(HTTP_CONNECTOR_NAME);
    connector.setPort(configuration.getHTTPPort());

    server.addConnector(connector);
    LOG.info("Configured plain HTTP connector on port: {}", configuration.getHTTPPort());
  }

  private void configureTLSConnector(Server server) {

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

  private void configureRewriteHandler(Server server) {

    RewriteHandler rh = new RewriteHandler();

    CompactPathRule compactPathRule = new CompactPathRule();

    RewriteRegexRule dropLegacyWebDAV = new RewriteRegexRule();
    dropLegacyWebDAV.setRegex("/webdav/(.*)");
    dropLegacyWebDAV.setReplacement("/$1");

    RewriteRegexRule dropLegacyFileTransfer = new RewriteRegexRule();
    dropLegacyFileTransfer.setRegex("/fileTransfer/(.*)");
    dropLegacyFileTransfer.setReplacement("/$1");

    rh.addRule(compactPathRule);
    rh.addRule(dropLegacyWebDAV);
    rh.addRule(dropLegacyFileTransfer);

    rh.setHandler(server.getHandler());

    InstrumentedEE10Handler ih = new InstrumentedEE10Handler(metricRegistry, "storm.http.handler");
    ih.setHandler(rh);
    server.setHandler(ih);
  }

  private void configureAccessLog(Server server) {
    RequestLogImpl rli = new RequestLogImpl();

    rli.setQuiet(true);
    String accessLogConfiguration = configuration.getAccessLogConfigurationPath();

    if (!StringUtils.hasText(accessLogConfiguration)) {
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

  @PostConstruct
  protected void after() {
    confLogger.logConfiguration(LOG);
  }
}
