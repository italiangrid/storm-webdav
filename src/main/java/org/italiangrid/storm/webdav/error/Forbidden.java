// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.error;

public class Forbidden extends StoRMWebDAVError {

  /** */
  private static final long serialVersionUID = 1L;

  public Forbidden(String message) {
    super(message);
  }
}
