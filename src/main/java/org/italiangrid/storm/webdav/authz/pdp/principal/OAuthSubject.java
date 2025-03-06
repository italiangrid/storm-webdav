// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.pdp.principal;

import java.net.URL;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class OAuthSubject implements PrincipalMatcher {

  final URL iss;
  final String sub;

  public OAuthSubject(URL issuer, String subject) {
    this.iss = issuer;
    this.sub = subject;
  }

  @Override
  public boolean matchesPrincipal(Authentication authentication) {
    if (!(authentication instanceof JwtAuthenticationToken)) {
      return false;
    }

    JwtAuthenticationToken authToken = (JwtAuthenticationToken) authentication;

    return iss.equals(authToken.getToken().getIssuer())
        && sub.equals(authToken.getToken().getSubject());
  }
}
