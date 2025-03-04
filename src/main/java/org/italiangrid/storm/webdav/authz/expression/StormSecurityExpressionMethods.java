// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.expression;

import org.italiangrid.storm.webdav.authz.VOMSVOAuthority;
import org.italiangrid.storm.webdav.authz.VOMSVOMapAuthority;
import org.springframework.security.core.Authentication;

public class StormSecurityExpressionMethods {

  final Authentication authentication;

  public StormSecurityExpressionMethods(Authentication authn) {
    this.authentication = authn;
  }

  public boolean isVOMSAuthenticated() {
    return authentication.getAuthorities().stream().anyMatch(VOMSVOAuthority.class::isInstance);
  }

  public boolean hasVoMapAuthorities() {
    return authentication.getAuthorities().stream().anyMatch(VOMSVOMapAuthority.class::isInstance);
  }
}
