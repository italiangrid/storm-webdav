// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.error;

public class StoRMWebDAVError extends RuntimeException {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  public StoRMWebDAVError() {

  }

  public StoRMWebDAVError(String message) {

    super(message);
  }

  public StoRMWebDAVError(Throwable cause) {

    super(cause);

  }

  public StoRMWebDAVError(String message, Throwable cause) {

    super(message, cause);

  }

}
