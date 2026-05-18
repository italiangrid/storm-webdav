// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer.error;

public class FileTooSmall extends TransferError {

  /** */
  private static final long serialVersionUID = 1L;

  public FileTooSmall(String message) {
    super(message);
  }
}
