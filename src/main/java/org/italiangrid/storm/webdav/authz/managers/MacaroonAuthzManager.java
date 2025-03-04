// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.managers;

import java.util.function.Supplier;

import org.italiangrid.storm.webdav.macaroon.MacaroonRequestFilter;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.Assert;

public class MacaroonAuthzManager implements AuthorizationManager<RequestAuthorizationContext> {

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
    Assert.notNull(authentication.get(), "authentication must not be null");
    Assert.notNull(requestAuthorizationContext, "requestAuthorizationContext must not be null");

    if (MacaroonRequestFilter.isMacaroonRequest(requestAuthorizationContext.getRequest())) {
      return new AuthorizationDecision(true);
    }
    return null;
  }

}
