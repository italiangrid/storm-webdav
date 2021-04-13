/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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

import static java.util.Objects.isNull;

import java.util.Map;
import java.util.Objects;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class AuthenticationUtils {

  private AuthenticationUtils() {
    // empty on purpose
  }

  public static String getPalatableSubject(Authentication authn) {
    if (Objects.isNull(authn) || authn instanceof AnonymousAuthenticationToken) {
      return "Anonymous user";
    } else if (authn instanceof OAuth2AuthenticationToken) {
      OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authn;
      Map<String, Object> attributes = authToken.getPrincipal().getAttributes();

      String subjectIssuer = String.format("%s @ %s", attributes.get("sub"), attributes.get("iss"));

      if (!isNull(attributes.get("name"))) {
        return (String) attributes.get("name");
      }

      return subjectIssuer;

    } else if (authn instanceof PreAuthenticatedAuthenticationToken) {
      return authn.getName();
    } else if (authn instanceof JwtAuthenticationToken) {
      JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authn;
      return String.format("%s @ %s", jwtToken.getToken().getSubject(),
          jwtToken.getToken().getIssuer());
    } else {
      return authn.getName();
    }
  }

}
