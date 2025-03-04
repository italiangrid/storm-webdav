// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.pdp.principal;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

public class AnyAuthenticatedUser implements PrincipalMatcher {

  @Override
  public boolean matchesPrincipal(Authentication authentication) {
    return authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
        && authentication.isAuthenticated();
  }

  @Override
  public String toString() {
    return "Any authenticated user";
  }
}
