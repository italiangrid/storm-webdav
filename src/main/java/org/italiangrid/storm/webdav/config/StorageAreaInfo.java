package org.italiangrid.storm.webdav.config;

import java.util.List;
import java.util.Set;

public interface StorageAreaInfo {

  public String name();

  public String rootPath();

  public String filesystemType();

  public List<String> accessPoints();

  public Set<String> vos();

  public Boolean anonymousReadEnabled();

  public Boolean authenticatedReadEnabled();
}
