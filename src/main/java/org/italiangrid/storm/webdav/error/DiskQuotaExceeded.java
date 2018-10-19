package org.italiangrid.storm.webdav.error;

public class DiskQuotaExceeded extends StoRMWebDAVError {
  
  private static final long serialVersionUID = 1L;

  public DiskQuotaExceeded(String message, Throwable cause) {
    super(message, cause);
  }

}
