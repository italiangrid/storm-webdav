package org.italiangrid.storm.webdav.tpc.transfer.impl;

import java.net.URI;

import com.google.common.collect.Multimap;

public class PutTransferRequestImpl extends TransferRequestImpl {

  public PutTransferRequestImpl(String path, URI uri, Multimap<String, String> xferHeaders,
      boolean verifyChecksum, boolean overwrite) {
    super(path, uri, xferHeaders, verifyChecksum, overwrite);
  }

  @Override
  public String toString() {
    return "PutTransferRequest [path=" + path + ", uri=" + uri + ", xferHeaders=" + xferHeaders
        + ", verifyChecksum=" + verifyChecksum + ", overwrite=" + overwrite + "]";
  }

}
