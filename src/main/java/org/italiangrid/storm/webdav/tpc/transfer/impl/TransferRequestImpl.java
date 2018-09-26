package org.italiangrid.storm.webdav.tpc.transfer.impl;

import java.net.URI;

import org.italiangrid.storm.webdav.tpc.transfer.TransferRequest;

import com.google.common.collect.Multimap;

public abstract class TransferRequestImpl implements TransferRequest {

  final String path;

  final URI uri;

  final Multimap<String, String> xferHeaders;

  final boolean verifyChecksum;

  final boolean overwrite;

  TransferRequestImpl(String path, URI uri, Multimap<String, String> xferHeaders,
      boolean verifyChecksum, boolean overwrite) {

    this.path = path;
    this.uri = uri;
    this.xferHeaders = xferHeaders;
    this.verifyChecksum = verifyChecksum;
    this.overwrite = overwrite;
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public URI remoteURI() {
    return uri;
  }

  @Override
  public Multimap<String, String> transferHeaders() {
    return xferHeaders;
  }

  @Override
  public boolean verifyChecksum() {
    return verifyChecksum;
  }

  @Override
  public boolean overwrite() {
    return overwrite;
  }


}
