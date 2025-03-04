// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer.impl;

import static java.lang.String.format;

import java.net.URI;

import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;

import com.google.common.collect.Multimap;

public class PutTransferRequestImpl extends TransferRequestImpl implements PutTransferRequest {

  public PutTransferRequestImpl(String uuid, String path, URI uri,
      Multimap<String, String> xferHeaders, SciTag scitag, boolean verifyChecksum,
      boolean overwrite) {
    super(uuid, path, uri, xferHeaders, scitag, verifyChecksum, overwrite);
  }

  @Override
  public String toString() {
    return "PutTransferRequest [uuid=" + uuid + ", path=" + path + ", uri=" + uri + ", xferHeaders="
        + xferHeaders + ", verifyChecksum=" + verifyChecksum + ", overwrite=" + overwrite + "]";
  }

  @Override
  public String statusString() {
    return format("Push xfer request %s status: %s", uuid, lastTransferStatus());
  }

}
