// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.error;


public class SameFileError extends StoRMWebDAVError {

  /**
   * 
   */
  private static final long serialVersionUID = 1503156218876662642L;

  public SameFileError(String message) {

    super(message);

  }

  public SameFileError(Throwable cause) {

    super(cause);

  }

  public SameFileError(String message, Throwable cause) {

    super(message, cause);

  }

}
