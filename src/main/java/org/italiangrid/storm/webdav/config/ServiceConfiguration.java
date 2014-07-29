package org.italiangrid.storm.webdav.config;



public interface ServiceConfiguration {
	
	public int getHTTPSPort();
	public int getHTTPPort();

	public String getCertificatePath();
	public String getPrivateKeyPath();
	public String getTrustStoreDir();
	public long getTrustStoreRefreshIntervalInSeconds();
	
	public int getMaxConnections();
	
	public int getMaxQueueSize();
	
	public int getConnectorMaxIdleTime();
	
	public String getSAConfigDir();
}
