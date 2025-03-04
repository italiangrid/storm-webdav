// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.error;

public class ResourceAlreadyExists extends StoRMWebDAVError {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  public ResourceAlreadyExists() {

  }

  public ResourceAlreadyExists(String message) {

    super(message);
  }

  public ResourceAlreadyExists(Throwable cause) {

    super(cause);
  }

  public ResourceAlreadyExists(String message, Throwable cause) {

    super(message, cause);
  }

}
