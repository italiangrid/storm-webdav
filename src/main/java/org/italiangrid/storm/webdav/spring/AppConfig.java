package org.italiangrid.storm.webdav.spring;

import org.italiangrid.storm.webdav.config.ConfigurationLogger;
import org.italiangrid.storm.webdav.config.DefaultConfigurationLogger;
import org.italiangrid.storm.webdav.config.SAConfigurationImpl;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ServiceEnvConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.server.ServerLifecycle;
import org.italiangrid.storm.webdav.server.WebDAVServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig {
	
	@Bean
	public ServiceConfiguration serviceConfiguration(){
		return ServiceEnvConfiguration.INSTANCE;
	}
	
	@Bean
	public StorageAreaConfiguration storageAreaConfiguration(){
		return new SAConfigurationImpl(serviceConfiguration());
	}
	
	@Bean
	public ServerLifecycle webdavServer(){
		return new WebDAVServer(serviceConfiguration(), storageAreaConfiguration());
	}
	
	@Bean
	public ConfigurationLogger configurationLogger(){
		return new DefaultConfigurationLogger(serviceConfiguration(), storageAreaConfiguration());
	}
	
}
