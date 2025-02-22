/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
