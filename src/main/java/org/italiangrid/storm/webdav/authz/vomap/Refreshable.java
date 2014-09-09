package org.italiangrid.storm.webdav.authz.vomap;

public interface Refreshable {

  public void refresh();

  public long getLastRefreshTime();

}
