// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer;

@FunctionalInterface
public interface TransferStatusCallback {
  
  void reportStatus(TransferRequest req, TransferStatus ts);

}
