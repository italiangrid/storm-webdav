// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver.error;

public class InvalidScopeError extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidScopeError(String message) {
    super(message);
  }
}
