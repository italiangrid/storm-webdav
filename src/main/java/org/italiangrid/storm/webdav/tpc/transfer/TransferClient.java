// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer;

import org.apache.hc.client5.http.ClientProtocolException;

public interface TransferClient {

  void handle(GetTransferRequest request, TransferStatusCallback status)
      throws ClientProtocolException;

  void handle(PutTransferRequest request, TransferStatusCallback status)
      throws ClientProtocolException;
}
