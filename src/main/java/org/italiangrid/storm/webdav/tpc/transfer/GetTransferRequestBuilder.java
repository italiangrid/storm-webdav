package org.italiangrid.storm.webdav.tpc.transfer;

import java.net.URI;

import org.italiangrid.storm.webdav.tpc.transfer.impl.GetTransferRequestImpl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class GetTransferRequestBuilder {

  String path;
  URI uri;

  boolean verifyChecksum = true;

  boolean overwrite = true;

  Multimap<String, String> headers = ArrayListMultimap.create();

  private GetTransferRequestBuilder() {
  }

  public GetTransferRequestBuilder path(String path) {
    this.path = path;
    return this;
  }

  public GetTransferRequestBuilder uri(URI uri) {
    this.uri = uri;
    return this;
  }

  public GetTransferRequestBuilder headers(Multimap<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public GetTransferRequestBuilder addHeader(String header, String value) {
    headers.put(header, value);
    return this;
  }
  
  public GetTransferRequestBuilder overwrite(boolean o) {
    overwrite = o;
    return this;
  }
  
  public GetTransferRequestBuilder verifyChecksum(boolean v) {
    verifyChecksum = v;
    return this;
  }

  public GetTransferRequest build() {
    return new GetTransferRequestImpl(path, uri, headers, verifyChecksum, overwrite);

  }

  public static GetTransferRequestBuilder create() {
    return new GetTransferRequestBuilder();
  }

}
