// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server;

public class TLSConnectorBuilderError extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public TLSConnectorBuilderError(Throwable cause) {
    super(cause);
  }

  public TLSConnectorBuilderError(String message, Throwable cause) {
    super(message, cause);
  }

  public TLSConnectorBuilderError(String message) {
    super(message);
  }

}
