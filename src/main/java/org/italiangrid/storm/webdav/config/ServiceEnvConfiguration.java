package org.italiangrid.storm.webdav.config;





public enum ServiceEnvConfiguration implements ServiceConfiguration {
	INSTANCE;	
	
	
	@Override
	public int getHTTPSPort() {
		return Integer.parseInt(ServiceConfigVariable.HTTPS_PORT.getValue());
	}

	@Override
	public int getHTTPPort() {
		return Integer.parseInt(ServiceConfigVariable.HTTP_PORT.getValue());
	}

	@Override
	public String getCertificatePath() {
		return ServiceConfigVariable.CERTIFICATE_PATH.getValue();
	}

	@Override
	public String getPrivateKeyPath() {
		return ServiceConfigVariable.PRIVATE_KEY_PATH.getValue();
	}

	@Override
	public String getTrustAnchorsDir() {
		return ServiceConfigVariable.TRUST_ANCHORS_DIR.getValue();
	}

	@Override
	public long getTrustAnchorsRefreshIntervalInSeconds() {
		return Long.parseLong(ServiceConfigVariable.TRUST_ANCHORS_REFRESH_INTERVAL.getValue());
	}

	@Override
	public int getMaxConnections() {
		return Integer.parseInt(ServiceConfigVariable.MAX_CONNECTIONS.getValue());
	}

	@Override
	public int getMaxQueueSize() {
		return Integer.parseInt(ServiceConfigVariable.MAX_QUEUE_SIZE.getValue());
	}

	@Override
	public int getConnectorMaxIdleTime() {
		return Integer.parseInt(ServiceConfigVariable.CONNECTOR_MAX_IDLE_TIME.getValue());
	}

	@Override
	public String getSAConfigDir() {
		return ServiceConfigVariable.SA_CONFIG_DIR.getValue();
	}

	@Override
	public String getLogConfigurationPath() {
		return ServiceConfigVariable.LOG_CONFIGURATION.getValue();
	}

	@Override
	public String getAccessLogConfigurationPath() {
		return ServiceConfigVariable.ACCESS_LOG_CONFIGURATION.getValue();
	}
	
}

enum ServiceConfigVariable {
	
	HTTPS_PORT("8443"),
	HTTP_PORT("8085"),
	CERTIFICATE_PATH("/etc/grid-security/storm-webdav/hostcert.pem"),
	PRIVATE_KEY_PATH("/etc/grid-security/storm-webdav/hostkey.pem"),
	TRUST_ANCHORS_DIR("/etc/grid-security/certificates"),
	TRUST_ANCHORS_REFRESH_INTERVAL("86400"),
	MAX_CONNECTIONS("300"),
	MAX_QUEUE_SIZE("900"),
	CONNECTOR_MAX_IDLE_TIME("30000"),
	SA_CONFIG_DIR("/etc/storm-webdav/sa"),
	LOG_CONFIGURATION(null),
	ACCESS_LOG_CONFIGURATION(null);
	
	private String defaultValue;
	
	private ServiceConfigVariable(String value) {

		this.defaultValue = value;
	}
	
	protected String getDefaultValue(){
		return defaultValue;
	}
	
	public String getEnvKey(){
		return String.format("STORM_WEBDAV_%s", name());
	}
	
	public String getValue(){
		String val = System.getenv(getEnvKey());
		if (val == null){
			return defaultValue;
		}
		
		return val;
	}

}
