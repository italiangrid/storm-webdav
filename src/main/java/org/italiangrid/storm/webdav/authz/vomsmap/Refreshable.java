package org.italiangrid.storm.webdav.authz.vomsmap;



public interface Refreshable {
	
	public void refresh();
	
	public long getLastRefreshTime();
	
}
