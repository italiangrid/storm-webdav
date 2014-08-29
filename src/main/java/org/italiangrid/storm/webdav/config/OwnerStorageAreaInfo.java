package org.italiangrid.storm.webdav.config;

import java.util.List;
import java.util.Set;

import org.aeonbits.owner.Config;


public interface OwnerStorageAreaInfo extends StorageAreaInfo, Config{
	
	@Override
	@DefaultValue("posix")
	public String filesystemType();
	
	@Override
	@Separator(",")
	public List<String> accessPoints();
	
	@Override
	@Separator(",")
	public Set<String> vos();
	
	@DefaultValue("false")
	public Boolean anonymousReadEnabled();
	
	@Override
	@DefaultValue("false")
	public Boolean authenticatedReadEnabled();
	
}