// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
import org.springframework.security.authorization.AuthorizationResult;
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

  /**
   * @deprecated To be remove in Spring Security 7
   */
  @Deprecated(forRemoval = true)
  @Override
  public AuthorizationDecision check(Supplier<Authentication> authentication,
      RequestAuthorizationContext requestAuthorizationContext) {
    if (authorize(authentication,
        requestAuthorizationContext) instanceof AuthorizationDecision authorizationDecision) {
      return authorizationDecision;
    }
    return null;
  }

  @Override
  public AuthorizationResult authorize(Supplier<Authentication> authentication,
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
