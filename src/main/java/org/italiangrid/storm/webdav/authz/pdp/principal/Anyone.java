// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.pdp.principal;

import org.springframework.security.core.Authentication;

public class Anyone implements PrincipalMatcher {

  @Override
  public boolean matchesPrincipal(Authentication authentication) {
    return true;
  }

  @Override
  public String toString() {
    return "Anyone";
  }
}
