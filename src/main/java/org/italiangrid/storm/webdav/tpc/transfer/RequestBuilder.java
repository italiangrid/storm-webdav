// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.net.URI;
import org.italiangrid.storm.webdav.scitag.SciTag;

public abstract class RequestBuilder<T> {

  String uuid;

  String path;

  URI uri;

  SciTag scitag;

  boolean verifyChecksum = true;

  boolean overwrite = true;

  Multimap<String, String> headers = ArrayListMultimap.create();

  protected RequestBuilder() {}

  public abstract T build();

  public RequestBuilder<T> uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  public RequestBuilder<T> path(String path) {
    this.path = path;
    return this;
  }

  public RequestBuilder<T> uri(URI uri) {
    this.uri = uri;
    return this;
  }

  public RequestBuilder<T> headers(Multimap<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public RequestBuilder<T> addHeader(String header, String value) {
    headers.put(header, value);
    return this;
  }

  public RequestBuilder<T> scitag(SciTag scitag) {
    this.scitag = scitag;
    return this;
  }

  public RequestBuilder<T> overwrite(boolean o) {
    overwrite = o;
    return this;
  }

  public RequestBuilder<T> verifyChecksum(boolean v) {
    verifyChecksum = v;
    return this;
  }
}
