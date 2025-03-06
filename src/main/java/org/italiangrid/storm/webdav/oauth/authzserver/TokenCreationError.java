// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver;

public class TokenCreationError extends RuntimeException {

  /** */
  private static final long serialVersionUID = 1L;

  public TokenCreationError(String message) {
    super(message);
  }

  public TokenCreationError(Throwable cause) {
    super(cause);
  }
}
