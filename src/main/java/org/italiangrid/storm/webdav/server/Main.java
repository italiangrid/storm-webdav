package org.italiangrid.storm.webdav.server;

import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
	
	public static final Logger log = LoggerFactory.getLogger(Main.class);

	static void dumpConfiguration(ServiceConfiguration config){
		
		
	}
	
	public static void main(String[] args) {

		ServiceConfiguration config = ConfigurationFactory.getServiceConfiguration();
		dumpConfiguration(config);
		
		WebDAVServer server = new WebDAVServer(config);
		server.start();

	}
}
