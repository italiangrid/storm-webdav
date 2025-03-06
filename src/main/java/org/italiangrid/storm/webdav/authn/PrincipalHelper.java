// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authn;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class PrincipalHelper {

  public static final String ANONYMOUS = "anonymous";

  private final Optional<URL> localAuthzServerIssuer;

  public PrincipalHelper(ServiceConfigurationProperties config) throws MalformedURLException {
    if (config.getAuthzServer().isEnabled()) {
      localAuthzServerIssuer = Optional.of(new URL(config.getAuthzServer().getIssuer()));
    } else {
      localAuthzServerIssuer = Optional.empty();
    }
  }

  public String getPrincipalAsString(Authentication authn) {
    if (authn == null || authn instanceof AnonymousAuthenticationToken) {
      return ANONYMOUS;
    } else if (authn instanceof OAuth2AuthenticationToken authToken) {
      Map<String, Object> attributes = authToken.getPrincipal().getAttributes();
      return String.format("%s@%s", attributes.get("sub"), attributes.get("iss"));
    } else if (authn instanceof PreAuthenticatedAuthenticationToken) {
      return authn.getName();
    } else if (authn instanceof JwtAuthenticationToken jwtToken) {
      if (localAuthzServerIssuer.isPresent()
          && localAuthzServerIssuer.get().equals(jwtToken.getToken().getIssuer())) {
        return jwtToken.getToken().getSubject();
      } else {
        return String.format(
            "%s@%s", jwtToken.getToken().getSubject(), jwtToken.getToken().getIssuer());
      }
    } else {
      return authn.getName();
    }
  }
}
