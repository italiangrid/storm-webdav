// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.managers;

import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public class ConsensusBasedManager implements AuthorizationManager<RequestAuthorizationContext> {

  public static final Logger LOG = LoggerFactory.getLogger(ConsensusBasedManager.class);

  private final List<AuthorizationManager<RequestAuthorizationContext>> managers;

  private final String name;

  public ConsensusBasedManager(
      String name, List<AuthorizationManager<RequestAuthorizationContext>> managers) {
    this.name = name;
    this.managers = managers;
  }

  /**
   * @deprecated To be remove in Spring Security 7
   */
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

  @Override
  public AuthorizationResult authorize(
      Supplier<Authentication> authentication,
      RequestAuthorizationContext requestAuthorizationContext) {
    int grant = 0;
    int notGrant = 0;

    for (AuthorizationManager<RequestAuthorizationContext> manager : managers) {
      AuthorizationResult result = manager.authorize(authentication, requestAuthorizationContext);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Voter: {}, returned: {}", manager, result);
      }

      if (result != null) {
        if (result.isGranted()) {
          grant++;
        } else {
          notGrant++;
        }
      }
    }

    if (grant == 0 && notGrant == 0) {
      return new AuthorizationDecision(false);
    } else {
      return new AuthorizationDecision(grant >= notGrant);
    }
  }

  @Override
  public String toString() {
    return name;
  }
}
