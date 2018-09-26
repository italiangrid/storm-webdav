package org.italiangrid.storm.webdav.tpc.transfer;

import java.util.Optional;

public class TransferStatus {
  
  enum Status {
    SCHEDULED,
    STARTED,
    ERROR,
    DONE
  }
  
  final Status status;
  final long transferByteCount;
  final Optional<String> errorMessage;
  
  private TransferStatus(Status s, long bc) {
    this.status = s;
    this.transferByteCount = bc;
    this.errorMessage = Optional.empty();
  }


  public Status getStatus() {
    return status;
  }


  public long getTransferByteCount() {
    return transferByteCount;
  }


  public Optional<String> getErrorMessage() {
    return errorMessage;
  }

}
