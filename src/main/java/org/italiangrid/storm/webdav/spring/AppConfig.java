package org.italiangrid.storm.webdav.spring;

import java.util.concurrent.TimeUnit;

import org.italiangrid.storm.webdav.authz.vomsmap.VOMSMapDetailServiceBuilder;
import org.italiangrid.storm.webdav.authz.vomsmap.VOMSMapDetailsService;
import org.italiangrid.storm.webdav.config.ConfigurationLogger;
import org.italiangrid.storm.webdav.config.DefaultConfigurationLogger;
import org.italiangrid.storm.webdav.config.SAConfigurationParser;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ServiceEnvConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.fs.DefaultFSStrategy;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.MetricsFSStrategyWrapper;
import org.italiangrid.storm.webdav.fs.attrs.DefaultExtendedFileAttributesHelper;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.ServerLifecycle;
import org.italiangrid.storm.webdav.server.WebDAVServer;
import org.italiangrid.utils.https.impl.canl.CANLListener;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.codahale.metrics.MetricRegistry;

import eu.emi.security.authn.x509.CrlCheckingMode;
import eu.emi.security.authn.x509.NamespaceCheckingMode;
import eu.emi.security.authn.x509.OCSPCheckingMode;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;

@Configuration
public class AppConfig {

	@Bean
	public ServiceConfiguration serviceConfiguration() {

		return ServiceEnvConfiguration.INSTANCE;
	}

	@Bean
	public StorageAreaConfiguration storageAreaConfiguration() {

		return new SAConfigurationParser(serviceConfiguration());
	}

	@Bean
	public ServerLifecycle webdavServer() {

		return new WebDAVServer(serviceConfiguration(), storageAreaConfiguration());
	}

	@Bean
	public ConfigurationLogger configurationLogger() {

		return new DefaultConfigurationLogger(serviceConfiguration(),
			storageAreaConfiguration());
	}

	@Bean
	public ExtendedAttributesHelper extendedAttributesHelper() {

		return new DefaultExtendedFileAttributesHelper();
	}

	@Bean
	@Scope("prototype")
	public FilesystemAccess filesystemAccess() {

		return new MetricsFSStrategyWrapper(new DefaultFSStrategy(
			extendedAttributesHelper()), metricRegistry());

	}

	@Bean
	public MetricRegistry metricRegistry() {

		return new MetricRegistry();
	}
	
	@Bean
	public VOMSMapDetailsService vomsMapDetailService(){
			
		VOMSMapDetailServiceBuilder builder = new VOMSMapDetailServiceBuilder(serviceConfiguration());
		return builder.build();
	}
	
	@Bean
	public X509CertChainValidatorExt canlCertChainValidator(){
		ServiceConfiguration configuration = serviceConfiguration();
		
		CANLListener l = new CANLListener();
		CertificateValidatorBuilder builder = new CertificateValidatorBuilder();
		
		long refreshInterval = TimeUnit
			.SECONDS.toMillis(configuration.getTrustAnchorsRefreshIntervalInSeconds());
		
		X509CertChainValidatorExt validator = builder
			.namespaceChecks(NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS_REQUIRE)
			.crlChecks(CrlCheckingMode.IF_VALID).ocspChecks(OCSPCheckingMode.IGNORE)
			.lazyAnchorsLoading(false).storeUpdateListener(l)
			.validationErrorListener(l)
			.trustAnchorsDir(configuration.getTrustAnchorsDir())
			.trustAnchorsUpdateInterval(refreshInterval)
			.build();
		
		return validator;
	}
	
}
