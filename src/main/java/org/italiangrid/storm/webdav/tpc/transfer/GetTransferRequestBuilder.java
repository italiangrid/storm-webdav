// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer;

import org.italiangrid.storm.webdav.tpc.transfer.impl.GetTransferRequestImpl;

public class GetTransferRequestBuilder extends RequestBuilder<GetTransferRequest> {


  public GetTransferRequest build() {
    return new GetTransferRequestImpl(uuid, path, uri, headers, scitag, verifyChecksum, overwrite);

  }

  public static GetTransferRequestBuilder create() {
    return new GetTransferRequestBuilder();
  }

}
