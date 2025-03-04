// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth;

import static java.lang.String.format;

import org.springframework.security.oauth2.jwt.JwtException;

public class UnknownTokenIssuerError extends JwtException {

  
  private static final String UNKNOWN_ISSUER_TEMPLATE = "Unknown token issuer: %s";
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public UnknownTokenIssuerError(String issuer) {
    super(format(UNKNOWN_ISSUER_TEMPLATE,issuer));
  }

}
