package org.italiangrid.storm.webdav.config;

import org.slf4j.Logger;


public class DefaultConfigurationLogger implements ConfigurationLogger {

	final ServiceConfiguration serviceConfiguration;
	final StorageAreaConfiguration saConfiguration;
	
	public DefaultConfigurationLogger(ServiceConfiguration sc, StorageAreaConfiguration saConf) {
		serviceConfiguration = sc;
		saConfiguration = saConf;
	}

	private void logServiceConfiguration(Logger logger){
		logger.info("## WebDAV service configuration");
		logger.info("Storage area configuration dir: {}", serviceConfiguration.getSAConfigDir());
		logger.info("Logging configuration path: {}", serviceConfiguration.getLogConfigurationPath());
		logger.info("HTTP port: {}", serviceConfiguration.getHTTPPort());
		logger.info("HTTPS port: {}", serviceConfiguration.getHTTPSPort());
		logger.info("Max connections: {}", serviceConfiguration.getMaxConnections());
		logger.info("Max connection queue size: {}", serviceConfiguration.getMaxQueueSize());
		logger.info("Service certificate path: {}",serviceConfiguration.getCertificatePath());
		logger.info("Service private key path: {}",serviceConfiguration.getPrivateKeyPath());
		logger.info("Trust anchors directory: {}", serviceConfiguration.getTrustAnchorsDir());
		logger.info("Trust anchors refresh intervals (seconds): {}", serviceConfiguration.getTrustAnchorsRefreshIntervalInSeconds());
		logger.info("VOMS Map files enabled: {}", serviceConfiguration.enableVOMSMapFiles());
		logger.info("VOMS Map files configuration directory: {}", serviceConfiguration.getVOMSMapFilesConfigDir());
		logger.info("VOMS Map files refresh interval (seconds): {}", serviceConfiguration.getVOMSMapFilesRefreshIntervalInSeconds());
	}
	
	private void logSAConfiguration(Logger logger){
		logger.info("## Storage Areas configuration");
		for (StorageAreaInfo sa: saConfiguration.getStorageAreaInfo()){
			logger.info("{}: {}",sa.name(), sa);
		}
	}
	
	@Override
	public void logConfiguration(Logger logger) {
		logServiceConfiguration(logger);
		logSAConfiguration(logger);
	}

}
