// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.pdp.principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class AuthorityHolder implements PrincipalMatcher {

  final GrantedAuthority authority;

  private AuthorityHolder(GrantedAuthority authority) {
    this.authority = authority;
  }

  public String toString() {
    return String.format("AuthorityHolder(%s)", authority.getAuthority());
  }

  public static AuthorityHolder fromAuthority(GrantedAuthority authority) {
    return new AuthorityHolder(authority);
  }

  @Override
  public boolean matchesPrincipal(Authentication authentication) {

    return authentication != null && authentication.getAuthorities()
      .stream()
      .anyMatch(a -> a.getAuthority().equals(authority.getAuthority()));
  }
}
