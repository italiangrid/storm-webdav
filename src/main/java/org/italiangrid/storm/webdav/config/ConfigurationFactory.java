package org.italiangrid.storm.webdav.config;


public class ConfigurationFactory {

	public static ServiceConfiguration getServiceConfiguration(){
		return ServiceEnvConfiguration.INSTANCE;
	}
	
	public static StorageAreaConfiguration getSAConfiguration(){
		return OwnerSAConfiguration.INSTANCE;
	}
}
