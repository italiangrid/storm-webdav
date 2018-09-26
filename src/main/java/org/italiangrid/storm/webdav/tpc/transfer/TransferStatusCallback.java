package org.italiangrid.storm.webdav.tpc.transfer;

@FunctionalInterface
public interface TransferStatusCallback {
  
  void reportStatus(TransferStatus ts);

}
