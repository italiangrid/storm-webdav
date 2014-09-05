package org.italiangrid.storm.webdav.config;



public interface ServiceConfiguration {
	
	public int getHTTPSPort();
	public int getHTTPPort();

	public String getCertificatePath();
	public String getPrivateKeyPath();
	public String getTrustAnchorsDir();
	
	public String getLogConfigurationPath();
	public String getAccessLogConfigurationPath();
	
	public long getTrustAnchorsRefreshIntervalInSeconds();
	
	public int getMaxConnections();
	
	public int getMaxQueueSize();
	
	public int getConnectorMaxIdleTime();
	
	public String getSAConfigDir();
	
	public boolean enableVOMSMapFiles();
	
	public String getVOMSMapFilesConfigDir();
	
}
