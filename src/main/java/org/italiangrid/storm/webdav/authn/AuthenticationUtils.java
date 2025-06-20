// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authn;

import java.util.Map;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public final class AuthenticationUtils {

  private AuthenticationUtils() {
    // empty on purpose
  }

  public static String getPalatableSubject(Authentication authn) {
    if (authn == null || authn instanceof AnonymousAuthenticationToken) {
      return "Anonymous user";
    } else if (authn instanceof OAuth2AuthenticationToken authToken) {
      Map<String, Object> attributes = authToken.getPrincipal().getAttributes();

      if (attributes.get("name") != null) {
        return (String) attributes.get("name");
      }

      return String.format("%s @ %s", attributes.get("sub"), attributes.get("iss"));
    } else if (authn instanceof PreAuthenticatedAuthenticationToken) {
      return authn.getName();
    } else if (authn instanceof JwtAuthenticationToken jwtToken) {
      return String.format(
          "%s @ %s", jwtToken.getToken().getSubject(), jwtToken.getToken().getIssuer());
    } else {
      return authn.getName();
    }
  }
}
