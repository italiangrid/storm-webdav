package org.italiangrid.storm.webdav.tpc.transfer.impl;

import java.net.URI;

import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;

import com.google.common.collect.Multimap;

public class GetTransferRequestImpl extends TransferRequestImpl implements GetTransferRequest {

  public GetTransferRequestImpl(String path, URI uri, Multimap<String, String> xferHeaders,
      boolean verifyChecksum, boolean overwrite) {
    super(path, uri, xferHeaders, verifyChecksum, overwrite);
  }

  @Override
  public String toString() {
    return "GetTransferRequest[path=" + path + ", uri=" + uri + ", xferHeaders=" + xferHeaders
        + ", verifyChecksum=" + verifyChecksum + ", overwrite=" + overwrite + "]";
  }

}
