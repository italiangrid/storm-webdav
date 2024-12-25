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
package org.italiangrid.storm.webdav.authz.voters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.error.StoRMIntializationError;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.StringUtils;

public class LocalAuthzVoter extends PathAuthzPdpVoterSupport {

  public static final Logger LOG = LoggerFactory.getLogger(LocalAuthzVoter.class);

  final URL localTokenIssuer;

  public LocalAuthzVoter(ServiceConfigurationProperties config, PathResolver resolver,
      PathAuthorizationPdp pdp, LocalURLService localUrlService) {
    super(config, resolver, pdp, localUrlService, true);
    try {
      localTokenIssuer = new URL(config.getAuthzServer().getIssuer());
    } catch (MalformedURLException e) {
      throw new StoRMIntializationError(e.getMessage());
    }
  }

  private boolean isLocalAuthzToken(JwtAuthenticationToken token) {
    return localTokenIssuer.equals(token.getToken().getIssuer())
        && StringUtils.hasText(token.getToken().getClaimAsString(DefaultJwtTokenIssuer.PATH_CLAIM));
  }

  @Override
  public int vote(Authentication authentication, FilterInvocation object,
      Collection<ConfigAttribute> attributes) {

    if (!(authentication instanceof JwtAuthenticationToken)) {
      return ACCESS_ABSTAIN;
    }

    JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
    if (!isLocalAuthzToken(token)) {
      return ACCESS_ABSTAIN;
    }

    return renderDecision(
        PathAuthorizationRequest.newAuthorizationRequest(object.getRequest(), authentication), LOG);
  }

}
