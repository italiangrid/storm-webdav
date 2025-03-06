// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer.error;

public class TransferError extends RuntimeException {

  /** */
  private static final long serialVersionUID = 1L;

  public TransferError(String message) {
    super(message);
  }

  public TransferError(String message, Throwable cause) {
    super(message, cause);
  }
}
