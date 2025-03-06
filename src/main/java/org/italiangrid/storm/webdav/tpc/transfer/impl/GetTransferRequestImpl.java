// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer.impl;

import static java.lang.String.format;

import com.google.common.collect.Multimap;
import java.net.URI;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;

public class GetTransferRequestImpl extends TransferRequestImpl implements GetTransferRequest {

  public GetTransferRequestImpl(
      String uuid,
      String path,
      URI uri,
      Multimap<String, String> xferHeaders,
      SciTag scitag,
      boolean verifyChecksum,
      boolean overwrite) {
    super(uuid, path, uri, xferHeaders, scitag, verifyChecksum, overwrite);
  }

  @Override
  public String toString() {
    return "GetTransferRequest[uuid="
        + uuid
        + ", path="
        + path
        + ", uri="
        + uri
        + ", xferHeaders="
        + xferHeaders
        + ", verifyChecksum="
        + verifyChecksum
        + ", overwrite="
        + overwrite
        + "]";
  }

  @Override
  public String statusString() {
    return format("Pull xfer request %s status: %s", uuid, lastTransferStatus());
  }
}
