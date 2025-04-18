// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.managers;

import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;

import java.util.function.Supplier;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import io.opentelemetry.instrumentation.annotations.WithSpan;

public class WlcgScopeAuthzManager extends PathAuthzPdpManagerSupport {

  public static final Logger LOG = LoggerFactory.getLogger(WlcgScopeAuthzManager.class);

  public WlcgScopeAuthzManager(
      ServiceConfigurationProperties config,
      PathResolver resolver,
      PathAuthorizationPdp pdp,
      LocalURLService localUrlService) {
    super(config, resolver, pdp, localUrlService, true);
  }

  /**
   * @deprecated To be remove in Spring Security 7
   */
  @WithSpan
  @Deprecated(forRemoval = true)
  @Override
  public AuthorizationDecision check(
      Supplier<Authentication> authentication,
      RequestAuthorizationContext requestAuthorizationContext) {
    if (authorize(authentication, requestAuthorizationContext)
        instanceof AuthorizationDecision authorizationDecision) {
      return authorizationDecision;
    }
    return null;
  }

  @WithSpan
  @Override
  public AuthorizationResult authorize(
      Supplier<Authentication> authentication,
      RequestAuthorizationContext requestAuthorizationContext) {

    if (!(authentication.get() instanceof JwtAuthenticationToken)) {
      return null;
    }

    final String requestPath = getRequestPath(requestAuthorizationContext.getRequest());
    StorageAreaInfo sa = resolver.resolveStorageArea(requestPath);

    if (sa == null) {
      return null;
    }

    if (!sa.wlcgScopeAuthzEnabled()) {
      return null;
    }

    return renderDecision(
        newAuthorizationRequest(requestAuthorizationContext.getRequest(), authentication.get()),
        LOG);
  }
}
