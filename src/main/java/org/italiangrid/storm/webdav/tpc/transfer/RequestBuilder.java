/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.italiangrid.storm.webdav.tpc.transfer;

import java.net.URI;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public abstract class RequestBuilder<T> {

  String uuid;

  String path;

  URI uri;

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

  public RequestBuilder<T> overwrite(boolean o) {
    overwrite = o;
    return this;
  }

  public RequestBuilder<T> verifyChecksum(boolean v) {
    verifyChecksum = v;
    return this;
  }
}
