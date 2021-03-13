/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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
package org.italiangrid.storm.webdav.authn;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
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
    if (Objects.isNull(authn) || authn instanceof AnonymousAuthenticationToken) {
      return "anonymous";
    } else if (authn instanceof OAuth2AuthenticationToken) {
      OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authn;
      Map<String, Object> attributes = authToken.getPrincipal().getAttributes();
      String subjectIssuer = String.format("%s@%s", attributes.get("sub"), attributes.get("iss"));
      return subjectIssuer;

    } else if (authn instanceof PreAuthenticatedAuthenticationToken) {
      return authn.getName();
    } else if (authn instanceof JwtAuthenticationToken) {
      JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authn;
      if (localAuthzServerIssuer.isPresent()
          && localAuthzServerIssuer.get().equals(jwtToken.getToken().getIssuer())) {
        return jwtToken.getToken().getSubject();
      } else {
        return String.format("%s@%s", jwtToken.getToken().getSubject(),
            jwtToken.getToken().getIssuer());
      }
    } else {
      return authn.getName();
    }
  }

}
