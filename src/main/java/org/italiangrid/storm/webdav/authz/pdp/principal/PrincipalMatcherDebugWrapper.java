// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.pdp.principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

public class PrincipalMatcherDebugWrapper implements PrincipalMatcher {

  public static final Logger LOG = LoggerFactory.getLogger(PrincipalMatcherDebugWrapper.class);

  final PrincipalMatcher delegate;

  public PrincipalMatcherDebugWrapper(PrincipalMatcher delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean matchesPrincipal(Authentication authentication) {
    boolean result = delegate.matchesPrincipal(authentication);
    if (LOG.isDebugEnabled()) {
      LOG.debug("{} matches authentication {}: {}", delegate, authentication, result);
    }
    return result;
  }

  @Override
  public String toString() {

    return delegate.toString();
  }
}
