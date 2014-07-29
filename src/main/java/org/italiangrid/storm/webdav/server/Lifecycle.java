package org.italiangrid.storm.webdav.server;


public interface Lifecycle {
	
	public void start();
	public void stop();
	public boolean isStarted();

}
