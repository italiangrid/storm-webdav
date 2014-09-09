package org.italiangrid.storm.webdav.server;

public interface ServerLifecycle {

  public void start();

  public void stop();

  public boolean isStarted();

}
