// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.error;

public class StoRMIntializationError extends StoRMWebDAVError {

  /** */
  private static final long serialVersionUID = 1L;

  public StoRMIntializationError(String message) {
    super(message);
  }

  public StoRMIntializationError(Throwable cause) {
    super(cause);
  }

  public StoRMIntializationError(String message, Throwable cause) {
    super(message, cause);
  }
}
