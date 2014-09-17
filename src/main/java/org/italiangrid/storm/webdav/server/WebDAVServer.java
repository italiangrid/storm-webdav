/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.italiangrid.storm.webdav.server;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContextListener;

import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.italiangrid.storm.webdav.authz.vomap.VOMapDetailsService;
import org.italiangrid.storm.webdav.config.ConfigurationLogger;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.metrics.MetricsContextListener;
import org.italiangrid.storm.webdav.metrics.StormMetricsReporter;
import org.italiangrid.storm.webdav.spring.web.MyLoaderListener;
import org.italiangrid.storm.webdav.spring.web.SecurityConfig;
import org.italiangrid.utils.https.JettyRunThread;
import org.italiangrid.utils.https.SSLOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.thymeleaf.TemplateEngine;

import ch.qos.logback.access.jetty.RequestLogImpl;
import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jetty8.InstrumentedHandler;
import com.codahale.metrics.jetty8.InstrumentedQueuedThreadPool;
import com.codahale.metrics.jetty8.InstrumentedSelectChannelConnector;
import com.codahale.metrics.jetty8.InstrumentedSslSelectChannelConnector;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.codahale.metrics.servlets.PingServlet;
import com.codahale.metrics.servlets.ThreadDumpServlet;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.impl.PEMCredential;
import eu.emi.security.authn.x509.impl.SocketFactoryCreator;

@Component
public class WebDAVServer implements ServerLifecycle, ApplicationContextAware {

  public static final Logger log = LoggerFactory.getLogger(WebDAVServer.class);

  public static final String HTTP_CONNECTOR_NAME = "storm-http";
  public static final String HTTPS_CONNECTOR_NAME = "storm-https";

  private boolean started;

  private Server jettyServer;

  private final ServiceConfiguration configuration;
  private final StorageAreaConfiguration saConfiguration;

  @Autowired
  private ConfigurationLogger confLogger;

  @Autowired
  private VOMapDetailsService vomsMapDetailsService;

  @Autowired
  private X509CertChainValidatorExt certChainValidator;

  @Autowired
  private ExtendedAttributesHelper extendedAttrsHelper;

  @Autowired
  private MetricRegistry metricRegistry;

  @Autowired
  private HealthCheckRegistry healthCheckRegistry;

  @Autowired
  private TemplateEngine templateEngine;

  @Autowired
  private PathResolver pathResolver;

  private HandlerCollection handlers = new HandlerCollection();

  private ApplicationContext applicationContext;

  @Autowired
  public WebDAVServer(ServiceConfiguration conf, StorageAreaConfiguration saConf) {

    configuration = conf;
    saConfiguration = saConf;
  }

  public synchronized void configureLogging() {

    String loggingConf = configuration.getLogConfigurationPath();

    if (loggingConf == null || loggingConf.trim().isEmpty()) {
      log.info("Logging conf null or empty, skipping logging reconfiguration.");
      return;
    }

    File f = new File(loggingConf);
    if (!f.exists() || !f.canRead()) {
      log.error("Error loading logging configuration: "
        + "{} does not exist or is not readable.", loggingConf);
      return;
    }

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    JoranConfigurator configurator = new JoranConfigurator();

    configurator.setContext(lc);
    lc.reset();

    try {
      configurator.doConfigure(loggingConf);
      StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

    } catch (JoranException e) {
      failAndExit("Error setting up the logging system", e);

    }

    log.info("Logging system reconfigured succesfully.");
  }

  private void logConfiguration() {

    confLogger.logConfiguration(log);
  }

  private void setupMetricsReporting() {

    final StormMetricsReporter reporter = StormMetricsReporter.forRegistry(
      metricRegistry).build();

    reporter.start(1, TimeUnit.MINUTES);

  }

  @Override
  public synchronized void start() {

    if (started) {
      throw new IllegalStateException("Server already started");
    }

    logConfiguration();

    configureJVMMetrics();

    setupMetricsReporting();

    startServer();
    started = true;
  }

  private SSLOptions getSSLOptions() {

    SSLOptions options = new SSLOptions();

    options.setCertificateFile(configuration.getCertificatePath());
    options.setKeyFile(configuration.getPrivateKeyPath());
    options.setTrustStoreDirectory(configuration.getTrustAnchorsDir());
    options
      .setTrustStoreRefreshIntervalInMsec(java.util.concurrent.TimeUnit.SECONDS
        .toMillis(configuration.getTrustAnchorsRefreshIntervalInSeconds()));

    options.setWantClientAuth(true);
    options.setNeedClientAuth(true);

    return options;

  }

  private void configureThreadPool(Server server) {

    InstrumentedQueuedThreadPool tp = new InstrumentedQueuedThreadPool(
      metricRegistry);

    tp.setMinThreads(5);
    tp.setMaxThreads(configuration.getMaxConnections());
    tp.setMaxQueued(configuration.getMaxQueueSize());
    tp.setMaxIdleTimeMs(configuration.getConnectorMaxIdleTimeInMsec());

    server.setThreadPool(tp);

  }

  private void configureSSLConnector(Server server) throws KeyStoreException,
    CertificateException, IOException {

    SSLOptions options = getSSLOptions();
    PEMCredential serviceCredentials = new PEMCredential(options.getKeyFile(),
      options.getCertificateFile(), options.getKeyPassword());

    SSLContext sslContext = SocketFactoryCreator.getSSLContext(
      serviceCredentials, certChainValidator, null);

    SslContextFactory factory = new SslContextFactory();
    factory.setSslContext(sslContext);

    factory.setWantClientAuth(true);
    factory.setNeedClientAuth(true);

    Connector connector = new InstrumentedSslSelectChannelConnector(
      metricRegistry, configuration.getHTTPSPort(), factory,
      Clock.defaultClock());

    server.addConnector(connector);
  }

  private void configurePlainConnector(Server server) {

    InstrumentedSelectChannelConnector httpConnector = new InstrumentedSelectChannelConnector(
      metricRegistry, configuration.getHTTPPort(), Clock.defaultClock());

    httpConnector.setMaxIdleTime(configuration.getConnectorMaxIdleTimeInMsec());
    httpConnector.setName(HTTP_CONNECTOR_NAME);
    server.addConnector(httpConnector);
  }

  private void configureJettyServer() throws MalformedURLException,
    IOException, KeyStoreException, CertificateException {

    jettyServer = new Server();
    jettyServer.setSendServerVersion(false);
    jettyServer.setSendDateHeader(false);

    configureThreadPool(jettyServer);
    configureSSLConnector(jettyServer);
    configurePlainConnector(jettyServer);

    configureHandlers();

    jettyServer.setDumpAfterStart(false);
    jettyServer.setDumpBeforeStop(false);
    jettyServer.setStopAtShutdown(true);

    jettyServer.addLifeCycleListener(JettyServerListener.INSTANCE);

  }

  private Handler configureSAHandler() {

    FilterHolder springSecurityFilter = new FilterHolder(
      new DelegatingFilterProxy("springSecurityFilterChain"));

    FilterHolder miltonFilter = new FilterHolder(new MiltonFilter(
      applicationContext.getBean(FilesystemAccess.class), extendedAttrsHelper,
      pathResolver));

    ServletHolder metricsServlet = new ServletHolder(MetricsServlet.class);
    ServletHolder pingServlet = new ServletHolder(PingServlet.class);
    ServletHolder threadDumpServlet = new ServletHolder(ThreadDumpServlet.class);

    PathResolver resolver = new DefaultPathResolver(saConfiguration);

    ServletHolder servlet = new ServletHolder(new StoRMServlet(resolver));
    ServletHolder index = new ServletHolder(new SAIndexServlet(saConfiguration,
      templateEngine));

    WebAppContext ch = new WebAppContext();
    ch.setContextPath("/");
    ch.setWar("/");
    ch.setThrowUnavailableOnStartupException(true);
    ch.setCompactPath(true);

    ch.setInitParameter("contextClass",
      AnnotationConfigWebApplicationContext.class.getName());

    ch.setInitParameter("contextConfigLocation", SecurityConfig.class.getName());

    ch.setInitParameter("org.eclipse.jetty.servlet.Default.acceptRanges",
      "true");
    ch.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
    ch.setInitParameter("org.eclipse.jetty.servlet.Default.aliases", "false");
    ch.setInitParameter("org.eclipse.jetty.servlet.Default.gzip", "false");

    EnumSet<DispatcherType> dispatchFlags = EnumSet.of(DispatcherType.REQUEST);

    ch.addServlet(index, "");

    ch.addFilter(springSecurityFilter, "/*", dispatchFlags);
    ch.addFilter(miltonFilter, "/*", dispatchFlags);

    ch.addServlet(metricsServlet, "/status/metrics");
    ch.addServlet(pingServlet, "/status/ping");
    ch.addServlet(threadDumpServlet, "/status/threads");
    ch.addServlet(servlet, "/*");

    ServletContextListener springContextListener = new MyLoaderListener(
      applicationContext);

    ch.addEventListener(new MetricsContextListener(metricRegistry));
    ch.addEventListener(springContextListener);

    return ch;

  }

  private Handler configureLogRequestHandler() {

    RequestLogHandler handler = new RequestLogHandler();

    RequestLogImpl rli = new RequestLogImpl();
    rli.setQuiet(true);
    String accessLogConf = configuration.getAccessLogConfigurationPath();

    if (accessLogConf == null || accessLogConf.trim().isEmpty()) {
      log
        .info("Access log configuration null or empty, keeping internal configuration.");
      rli.setResource("/access.xml");
    } else {
      rli.setFileName(accessLogConf);
    }

    handler.setRequestLog(rli);
    return handler;
  }

  private void configureHandlers() throws MalformedURLException, IOException {

    handlers.addHandler(configureMetricsHandler());
    handlers.addHandler(configureSAHandler());
    handlers.addHandler(configureLogRequestHandler());

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

    rh.setHandler(handlers);

    InstrumentedHandler ih = new InstrumentedHandler(metricRegistry, rh,
      "storm.http.handler");

    jettyServer.setHandler(ih);

  }

  private Handler configureMetricsHandler() {

    ServletHolder metricsServlet = new ServletHolder(MetricsServlet.class);
    ServletHolder pingServlet = new ServletHolder(PingServlet.class);
    ServletHolder threadDumpServlet = new ServletHolder(ThreadDumpServlet.class);

    ServletContextHandler ch = new ServletContextHandler();

    ch.setContextPath("/status");

    ch.setCompactPath(true);
    ch.addEventListener(new MetricsContextListener(metricRegistry));

    ch.addServlet(metricsServlet, "/metrics");
    ch.addServlet(pingServlet, "/ping");
    ch.addServlet(threadDumpServlet, "/threads");

    return ch;

  }

  private void registerMetricSet(String prefix, MetricSet metricSet) {

    for (Entry<String, Metric> entry : metricSet.getMetrics().entrySet()) {
      if (entry.getValue() instanceof MetricSet) {
        registerMetricSet(prefix + "." + entry.getKey(),
          (MetricSet) entry.getValue());
      } else {
        metricRegistry.register(prefix + "." + entry.getKey(),
          (Metric) entry.getValue());
      }
    }

  }

  private void configureJVMMetrics() {

    registerMetricSet("jvm.gc", new GarbageCollectorMetricSet());
    registerMetricSet("jvm.memory", new MemoryUsageGaugeSet());
    registerMetricSet("jvm.threads", new ThreadStatesGaugeSet());
  }

  private void failAndExit(String message, Throwable cause) {

    log.error("{}:{}", message, cause.getMessage(), cause);
    System.exit(1);
  }

  private void startServer() {

    try {
      configureJettyServer();
      JettyRunThread rt = new JettyRunThread(jettyServer);
      rt.start();

    } catch (Exception e) {
      failAndExit("Error configuring Jetty server", e);
    }

  }

  @Override
  public synchronized void stop() {

    if (!started) {
      throw new IllegalStateException("Server not started");
    }

  }

  @Override
  public synchronized boolean isStarted() {

    return started;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext)
    throws BeansException {

    this.applicationContext = applicationContext;

  }

}
