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

import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public class ConsensusBasedManager implements AuthorizationManager<RequestAuthorizationContext> {

  public static final Logger LOG = LoggerFactory.getLogger(ConsensusBasedManager.class);

  private final List<AuthorizationManager<RequestAuthorizationContext>> managers;

  private final String name;

  public ConsensusBasedManager(String name,
      List<AuthorizationManager<RequestAuthorizationContext>> managers) {
    this.name = name;
    this.managers = managers;
  }

  @Override
  public AuthorizationDecision check(Supplier<Authentication> authentication,
      RequestAuthorizationContext requestAuthorizationContext) {
    int grant = 0;
    int notGrant = 0;

    for (AuthorizationManager<RequestAuthorizationContext> manager : managers) {
      AuthorizationDecision result = manager.check(authentication, requestAuthorizationContext);

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
