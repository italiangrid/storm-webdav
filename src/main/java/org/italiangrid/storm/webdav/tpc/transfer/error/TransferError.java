package org.italiangrid.storm.webdav.tpc.transfer.error;

public class TransferError extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public TransferError(String message) {
    super(message);
  }

  public TransferError(String message, Throwable cause) {
    super(message, cause);
  }

}
