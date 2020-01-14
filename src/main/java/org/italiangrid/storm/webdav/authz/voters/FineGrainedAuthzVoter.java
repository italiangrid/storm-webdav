/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;

import java.util.Collection;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

public class FineGrainedAuthzVoter implements AccessDecisionVoter<FilterInvocation> {
  public static final Logger LOG = LoggerFactory.getLogger(FineGrainedAuthzVoter.class);

  final PathAuthorizationPdp pdp;

  public FineGrainedAuthzVoter(PathAuthorizationPdp pdp) {
    this.pdp = pdp;
  }

  @Override
  public boolean supports(ConfigAttribute attribute) {
    return false;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return FilterInvocation.class.isAssignableFrom(clazz);
  }

  public void logAuthorizationSuccess(FilterInvocation invocation, PathAuthorizationResult result) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Authorization SUCCESS for invocation {} authorized by policy {}", invocation,
          result.getPolicy().get());
    }
  }

  public void logAuthorizationFailure(FilterInvocation invocation, PathAuthorizationResult result) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Authorization FAILURE for invocation {}. Policy: {}", invocation,
          result.getPolicy());
    }
  }


  @Override
  public int vote(Authentication authentication, FilterInvocation invocation,
      Collection<ConfigAttribute> attributes) {

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(invocation.getHttpRequest(), authentication));

    if (PathAuthorizationResult.Decision.PERMIT.equals(result.getDecision())) {
      logAuthorizationSuccess(invocation, result);
      return ACCESS_GRANTED;
    }

    logAuthorizationFailure(invocation, result);
    return ACCESS_DENIED;
  }

}
