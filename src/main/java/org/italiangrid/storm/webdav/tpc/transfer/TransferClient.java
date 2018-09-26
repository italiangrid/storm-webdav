package org.italiangrid.storm.webdav.tpc.transfer;

import org.apache.http.client.ClientProtocolException;

public interface TransferClient {

  void handle(GetTransferRequest request, TransferStatusCallback status) throws ClientProtocolException;
  
  void handle(PutTransferRequest request, TransferStatusCallback status) throws ClientProtocolException;

}
