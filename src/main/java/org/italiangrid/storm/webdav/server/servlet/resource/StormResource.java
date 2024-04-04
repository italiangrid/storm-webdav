package org.italiangrid.storm.webdav.server.servlet.resource;

import java.io.IOException;

public interface StormResource {

  public boolean isOnline() throws IOException;

  public boolean isStub() throws IOException;

  public boolean hasMigrated() throws IOException;

  public boolean hasInProgressRecall() throws IOException;

}
