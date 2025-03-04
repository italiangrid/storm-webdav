// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer;

import org.italiangrid.storm.webdav.tpc.transfer.impl.PutTransferRequestImpl;

public class PutTransferRequestBuilder extends RequestBuilder<PutTransferRequest> {

  private PutTransferRequestBuilder() {
    // empty constructor
  }


  @Override
  public PutTransferRequest build() {
    return new PutTransferRequestImpl(uuid, path, uri, headers, scitag, verifyChecksum, overwrite);
  }

  public static PutTransferRequestBuilder create() {
    return new PutTransferRequestBuilder();
  }

}
