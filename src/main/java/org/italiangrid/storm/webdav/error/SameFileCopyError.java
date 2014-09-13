package org.italiangrid.storm.webdav.error;


public class SameFileCopyError extends StoRMWebDAVError {

  /**
   * 
   */
  private static final long serialVersionUID = 1503156218876662642L;

  public SameFileCopyError(String message) {

    super(message);

  }

  public SameFileCopyError(Throwable cause) {

    super(cause);

  }

  public SameFileCopyError(String message, Throwable cause) {

    super(message, cause);

  }

}
