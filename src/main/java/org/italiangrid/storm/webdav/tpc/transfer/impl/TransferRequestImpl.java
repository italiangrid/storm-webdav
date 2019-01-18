/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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
package org.italiangrid.storm.webdav.tpc.transfer.impl;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.italiangrid.storm.webdav.tpc.transfer.TransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;

import com.google.common.collect.Multimap;

public abstract class TransferRequestImpl implements TransferRequest {

  final String uuid;

  final String path;

  final URI uri;

  final Multimap<String, String> xferHeaders;

  final boolean verifyChecksum;

  final boolean overwrite;

  private Optional<TransferStatus> lastTransferStatus = Optional.empty();

  TransferRequestImpl(String path, URI uri, Multimap<String, String> xferHeaders,
      boolean verifyChecksum, boolean overwrite) {

    this.uuid = UUID.randomUUID().toString();
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

  @Override
  public void setTransferStatus(TransferStatus status) {
    this.lastTransferStatus = Optional.of(status);
  }

  @Override
  public Optional<TransferStatus> lastTransferStatus() {
    return lastTransferStatus;
  }

  @Override
  public String uuid() {
    return uuid;
  }
}
