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

  @Override
  public AuthorizationResult authorize(
      Supplier<? extends Authentication> authentication,
      RequestAuthorizationContext requestAuthorizationContext) {
    Assert.notNull(authentication.get(), "authentication must not be null");
    Assert.notNull(requestAuthorizationContext, "requestAuthorizationContext must not be null");

    if (MacaroonRequestFilter.isMacaroonRequest(requestAuthorizationContext.getRequest())) {
      return new AuthorizationDecision(true);
    }
    return null;
  }
}
