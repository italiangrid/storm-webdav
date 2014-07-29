package org.italiangrid.storm.webdav.config;

import java.util.List;

import org.aeonbits.owner.Config;


public interface OwnerStorageAreaInfo extends StorageAreaInfo, Config{
	
	@DefaultValue("posix")
	public String filesystemType();
	
	@Separator(",")
	public List<String> accessPoints();
	
}