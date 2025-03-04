// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc;

public class URLResolutionError extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public URLResolutionError(String message) {
    super(message);
  }

  public URLResolutionError(Throwable cause) {
    super(cause);
  }

}
