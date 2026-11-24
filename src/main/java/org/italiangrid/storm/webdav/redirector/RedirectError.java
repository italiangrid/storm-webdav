// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.redirector;

import org.italiangrid.storm.webdav.error.StoRMWebDAVError;

public class RedirectError extends StoRMWebDAVError {

  /** */
  private static final long serialVersionUID = 1L;

  public RedirectError() {
    super();
  }

  public RedirectError(String message) {
    super(message);
  }
}
