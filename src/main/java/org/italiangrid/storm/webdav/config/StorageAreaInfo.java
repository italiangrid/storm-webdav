package org.italiangrid.storm.webdav.config;

import java.util.List;

public interface StorageAreaInfo {

	public String name();

	public String rootPath();

	public String filesystemType();

	public List<String> accessPoints();

}
