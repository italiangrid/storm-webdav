// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.utils;

public class OidcConfigurationResolutionError extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public OidcConfigurationResolutionError(String message) {
    super(message);
  }
  
  public OidcConfigurationResolutionError(String message, Throwable cause) {
    super(message, cause);

  }
}
