package org.italiangrid.storm.webdav.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.italiangrid.storm.webdav.config.ConfigurationFactory;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.utils.https.JettyRunThread;
import org.italiangrid.utils.https.SSLOptions;
import org.italiangrid.utils.https.ServerFactory;
import org.italiangrid.utils.https.impl.canl.CANLListener;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.access.jetty.RequestLogImpl;
import eu.emi.security.authn.x509.CrlCheckingMode;
import eu.emi.security.authn.x509.NamespaceCheckingMode;
import eu.emi.security.authn.x509.OCSPCheckingMode;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;

public class WebDAVServer implements Lifecycle{

	public static final Logger log = LoggerFactory.getLogger(WebDAVServer.class);
	
	public static final String HTTP_CONNECTOR_NAME = "storm-http";
	public static final String HTTPS_CONNECTOR_NAME = "storm-https";
	
	private boolean started;

	private Server jettyServer;
	private ServiceConfiguration configuration;
	
	
	private HandlerCollection handlers = new HandlerCollection();

	public WebDAVServer(ServiceConfiguration conf) {

		configuration = conf;
		
	}

	
	@Override
	public synchronized void start() {

		if (started) {
			throw new IllegalStateException("Server already started");
		}

		startServer();
		started = true;
	}

	private SSLOptions getSSLOptions() {

		SSLOptions options = new SSLOptions();

		options.setCertificateFile(configuration.getCertificatePath());
		options.setKeyFile(configuration.getPrivateKeyPath());
		options.setTrustStoreDirectory(configuration.getTrustStoreDir());
		options
			.setTrustStoreRefreshIntervalInMsec(java.util.concurrent.TimeUnit.SECONDS
				.toMillis(configuration.getTrustStoreRefreshIntervalInSeconds()));

		options.setWantClientAuth(true);
		options.setNeedClientAuth(true);
		
		return options;

	}

	
	private X509CertChainValidatorExt buildValidator(){
		
		SSLOptions options = getSSLOptions();
		
		CANLListener l = new CANLListener();
		CertificateValidatorBuilder builder = new CertificateValidatorBuilder();
		
		X509CertChainValidatorExt validator = builder
			.namespaceChecks(NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS_REQUIRE)
			.crlChecks(CrlCheckingMode.IF_VALID)
			.ocspChecks(OCSPCheckingMode.IGNORE)
			.lazyAnchorsLoading(false)
			.storeUpdateListener(l)
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
		
		configureConnectors();
		configureHandlers();
		
		jettyServer.setDumpAfterStart(false);
		jettyServer.setDumpBeforeStop(false);
		jettyServer.setStopAtShutdown(true);
		
		jettyServer.addLifeCycleListener(JettyServerListener.INSTANCE);
		

	}

	private void configureConnectors() {

		SelectChannelConnector httpConnector = new SelectChannelConnector();
		httpConnector.setPort(configuration.getHTTPPort());
		httpConnector.setMaxIdleTime(configuration.getConnectorMaxIdleTime());
		httpConnector.setName(HTTP_CONNECTOR_NAME);
		jettyServer.addConnector(httpConnector);
		
	}
	
	
	private Handler configureStorageAreaHandler(String rootPath, String accessPoint){
		
		ServletHolder servlet = new ServletHolder(DefaultServlet.class);
		
		FilterHolder securityFilter = new FilterHolder(SecurityFilter.class);
		FilterHolder miltonFilter = new FilterHolder(MiltonFilter.class);
		
		WebAppContext ch = new WebAppContext();
		
		ch.setContextPath(accessPoint);
		ch.setWar("/");
		ch.setThrowUnavailableOnStartupException(true);
		ch.setCompactPath(true);
		
		ch.setInitParameter("org.eclipse.jetty.servlet.Default.resourceBase", rootPath);
		ch.setInitParameter(MiltonFilter.SA_ROOT_PATH, rootPath);
		
		ch.setInitParameter("org.eclipse.jetty.servlet.Default.acceptRanges", "true");
		ch.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
		ch.setInitParameter("org.eclipse.jetty.servlet.Default.aliases", "false");
		
		EnumSet<DispatcherType> dispatchFlags = EnumSet.of(DispatcherType.REQUEST);
		ch.addServlet(servlet, "/*");
		ch.addFilter(securityFilter, "/*", dispatchFlags);
		ch.addFilter(miltonFilter, "/*", dispatchFlags);
		
		return ch;
		
	}
	
	private Handler configureLogRequestHandler(){
		RequestLogHandler handler = new RequestLogHandler();
		
		RequestLogImpl rli = new RequestLogImpl();
		rli.setQuiet(true);
		rli.setResource("/access.xml");
		handler.setRequestLog(rli);
		return handler;
	}

	
	
	private void configureHandlers() throws MalformedURLException, IOException {	
		
		
		List<StorageAreaInfo> sas = ConfigurationFactory.getSAConfiguration().getStorageAreaInfo();
		
		for (StorageAreaInfo sa: sas){
			
			for (String ap: sa.accessPoints()){
				handlers.addHandler(configureStorageAreaHandler(sa.rootPath(), ap));
			}
			
		}
		
		handlers.addHandler(configureLogRequestHandler());
		
		jettyServer.setHandler(handlers);
		
	}

	private void failAndExit(String message, Throwable cause){
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

}
