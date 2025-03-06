// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.vomap;

import org.italiangrid.storm.webdav.error.StoRMIntializationError;

public class VOMapFilesConfigurationError extends StoRMIntializationError {

  /** */
  private static final long serialVersionUID = 1L;

  public VOMapFilesConfigurationError(String message) {
    super(message);
  }

  public VOMapFilesConfigurationError(Throwable cause) {
    super(cause);
  }
}
