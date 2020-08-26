package org.italiangrid.storm.webdav.error;

import org.italiangrid.storm.webdav.milton.StoRMDirectoryResource;

public class DirectoryNotEmpty extends StoRMWebDAVError {


  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public DirectoryNotEmpty(StoRMDirectoryResource r) {
    super(String.format("Directory is not empty: %s", r.getName()));
  }

}
