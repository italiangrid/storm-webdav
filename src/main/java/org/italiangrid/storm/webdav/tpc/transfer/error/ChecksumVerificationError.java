package org.italiangrid.storm.webdav.tpc.transfer.error;

public class ChecksumVerificationError extends TransferError {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ChecksumVerificationError(String message) {
    super(message);
  }

}
