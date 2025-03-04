// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.error;

public class ResourceNotFound extends StoRMWebDAVError {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  public ResourceNotFound() {

  }

  public ResourceNotFound(String arg0) {

    super(arg0);
  }

  public ResourceNotFound(Throwable arg0) {

    super(arg0);

  }

  public ResourceNotFound(String arg0, Throwable arg1) {

    super(arg0, arg1);

  }

}
