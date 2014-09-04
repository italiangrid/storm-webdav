package org.italiangrid.storm.webdav.server;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContextListener;

import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.italiangrid.storm.webdav.config.ConfigurationLogger;
import org.italiangrid.storm.webdav.config.Constants;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.utils.https.JettyRunThread;
import org.italiangrid.utils.https.SSLOptions;
import org.italiangrid.utils.https.ServerFactory;
import org.italiangrid.utils.https.impl.canl.CANLListener;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import ch.qos.logback.access.jetty.RequestLogImpl;
import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

import eu.emi.security.authn.x509.CrlCheckingMode;
import eu.emi.security.authn.x509.NamespaceCheckingMode;
import eu.emi.security.authn.x509.OCSPCheckingMode;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;

@Component
public class WebDAVServer implements ServerLifecycle, ApplicationContextAware {

	public static final Logger log = LoggerFactory.getLogger(WebDAVServer.class);

	public static final String HTTP_CONNECTOR_NAME = "storm-http";
	public static final String HTTPS_CONNECTOR_NAME = "storm-https";
	
	public static final String METRICS_LOGGER_NAME = "storm-metrics-logger";

	private boolean started;

	private Server jettyServer;

	private final ServiceConfiguration configuration;
	private final StorageAreaConfiguration saConfiguration;

	@Autowired
	private ConfigurationLogger confLogger;

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

	private void setupMetricsReporting(){
		final Slf4jReporter reporter = Slf4jReporter
			.forRegistry(applicationContext.getBean(MetricRegistry.class))
			.outputTo(LoggerFactory.getLogger(METRICS_LOGGER_NAME))
			.convertRatesTo(TimeUnit.SECONDS)
			.convertDurationsTo(TimeUnit.MILLISECONDS)
			.build();
		
		reporter.start(1, TimeUnit.MINUTES);
	}
	
	
	@Override
	public synchronized void start() {

		if (started) {
			throw new IllegalStateException("Server already started");
		}

		// configureLogging();
		logConfiguration();
		
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

	private X509CertChainValidatorExt buildValidator() {

		SSLOptions options = getSSLOptions();

		CANLListener l = new CANLListener();
		CertificateValidatorBuilder builder = new CertificateValidatorBuilder();

		X509CertChainValidatorExt validator = builder
			.namespaceChecks(NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS_REQUIRE)
			.crlChecks(CrlCheckingMode.IF_VALID).ocspChecks(OCSPCheckingMode.IGNORE)
			.lazyAnchorsLoading(false).storeUpdateListener(l)
			.validationErrorListener(l)
			.trustAnchorsDir(options.getTrustStoreDirectory())
			.trustAnchorsUpdateInterval(options.getTrustStoreRefreshIntervalInMsec())
			.build();

		return validator;

	}

	private void configureJettyServer() throws MalformedURLException, IOException {

		X509CertChainValidatorExt validator = buildValidator();

		int maxConnections = configuration.getMaxConnections();

		int maxRequestQueueSize = configuration.getMaxQueueSize();

		jettyServer = ServerFactory.newServer(null, configuration.getHTTPSPort(),
			getSSLOptions(), validator, maxConnections, maxRequestQueueSize);

		// HTTP connector
		SelectChannelConnector httpConnector = new SelectChannelConnector();
		httpConnector.setPort(configuration.getHTTPPort());
		httpConnector.setMaxIdleTime(configuration.getConnectorMaxIdleTime());
		httpConnector.setName(HTTP_CONNECTOR_NAME);
		jettyServer.addConnector(httpConnector);

		configureHandlers();

		jettyServer.setDumpAfterStart(false);
		jettyServer.setDumpBeforeStop(false);
		jettyServer.setStopAtShutdown(true);

		jettyServer.addLifeCycleListener(JettyServerListener.INSTANCE);

	}

	private WebApplicationContext buildContext() {

		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.setConfigLocation("org.italiangrid.storm.webdav.spring.web");
		return context;

	}

	private Handler configureStorageAreaHandler(StorageAreaInfo sa,
		String accessPoint) {

		ServletHolder servlet = new ServletHolder(DefaultServlet.class);

		FilterHolder springSecurityFilter = new FilterHolder(
			new DelegatingFilterProxy("springSecurityFilterChain"));

		FilterHolder securityFilter = new FilterHolder(SecurityFilter.class);
		FilterHolder miltonFilter = new FilterHolder(new MiltonFilter(
			applicationContext.getBean(FilesystemAccess.class),
			applicationContext.getBean(ExtendedAttributesHelper.class)));

		ServletContextListener springContextListener = new ContextLoaderListener(
			buildContext());

		WebAppContext ch = new WebAppContext();

		ch.setContextPath(accessPoint);
		ch.setWar("/");
		ch.setThrowUnavailableOnStartupException(true);
		ch.setCompactPath(true);

		ch.setInitParameter("org.eclipse.jetty.servlet.Default.resourceBase",
			sa.rootPath());
		ch.setInitParameter(MiltonFilter.SA_ROOT_PATH, sa.rootPath());

		ch.setInitParameter("org.eclipse.jetty.servlet.Default.acceptRanges",
			"true");
		ch.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
		ch.setInitParameter("org.eclipse.jetty.servlet.Default.aliases", "false");

		ch.setAttribute(Constants.SA_CONF_KEY, sa);

		EnumSet<DispatcherType> dispatchFlags = EnumSet.of(DispatcherType.REQUEST);
		ch.addServlet(servlet, "/*");
		ch.addFilter(springSecurityFilter, "/*", dispatchFlags);
		ch.addFilter(securityFilter, "/*", dispatchFlags);
		ch.addFilter(miltonFilter, "/*", dispatchFlags);

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

		List<StorageAreaInfo> sas = saConfiguration.getStorageAreaInfo();

		for (StorageAreaInfo sa : sas) {

			for (String ap : sa.accessPoints()) {
				handlers.addHandler(configureStorageAreaHandler(sa, ap));
			}

		}

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
		
		jettyServer.setHandler(rh);

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
