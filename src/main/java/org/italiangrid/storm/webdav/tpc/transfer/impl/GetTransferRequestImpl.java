/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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

import static java.lang.String.format;

import java.net.URI;

import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;

import com.google.common.collect.Multimap;

public class GetTransferRequestImpl extends TransferRequestImpl implements GetTransferRequest {

  public GetTransferRequestImpl(String uuid, String path, URI uri,
      Multimap<String, String> xferHeaders,
      boolean verifyChecksum, boolean overwrite) {
    super(uuid, path, uri, xferHeaders, verifyChecksum, overwrite);
  }

  @Override
  public String toString() {
    return "GetTransferRequest[uuid=" + uuid + ", path=" + path + ", uri=" + uri + ", xferHeaders="
        + xferHeaders + ", verifyChecksum=" + verifyChecksum + ", overwrite=" + overwrite + "]";
  }

  @Override
  public String statusString() {
    return format("Pull xfer request %s status: %s", uuid, lastTransferStatus());
  }

}
