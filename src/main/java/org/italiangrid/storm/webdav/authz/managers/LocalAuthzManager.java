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
package org.italiangrid.storm.webdav.authz.managers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.error.StoRMIntializationError;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.StringUtils;

public class LocalAuthzManager extends PathAuthzPdpManagerSupport {

  public static final Logger LOG = LoggerFactory.getLogger(LocalAuthzManager.class);

  final URI localTokenIssuer;

  public LocalAuthzManager(ServiceConfigurationProperties config, PathResolver resolver,
      PathAuthorizationPdp pdp, LocalURLService localUrlService) {
    super(config, resolver, pdp, localUrlService, true);
    try {
      localTokenIssuer = new URI(config.getAuthzServer().getIssuer());
    } catch (URISyntaxException e) {
      throw new StoRMIntializationError(e.getMessage());
    }
  }

  private boolean isLocalAuthzToken(JwtAuthenticationToken token) {
    try {
      return localTokenIssuer.equals(token.getToken().getIssuer().toURI())
        && StringUtils.hasText(token.getToken().getClaimAsString(DefaultJwtTokenIssuer.PATH_CLAIM));
    } catch (URISyntaxException e) {
      LOG.warn("{}", e.getMessage());
      return false;
    }
  }

  @Override
  public AuthorizationDecision check(Supplier<Authentication> authentication,
      RequestAuthorizationContext requestAuthorizationContext) {

    if (!(authentication.get() instanceof JwtAuthenticationToken)) {
      return null;
    }

    JwtAuthenticationToken token = (JwtAuthenticationToken) authentication.get();
    if (!isLocalAuthzToken(token)) {
      return null;
    }

    return renderDecision(PathAuthorizationRequest.newAuthorizationRequest(
        requestAuthorizationContext.getRequest(), authentication.get()), LOG);
  }

}
