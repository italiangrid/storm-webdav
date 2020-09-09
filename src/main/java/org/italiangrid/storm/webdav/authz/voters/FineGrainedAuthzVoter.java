/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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

import static java.util.Objects.isNull;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.authz.util.MatcherUtils;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

public class FineGrainedAuthzVoter implements AccessDecisionVoter<FilterInvocation>, MatcherUtils {
  public static final Logger LOG = LoggerFactory.getLogger(FineGrainedAuthzVoter.class);

  final PathAuthorizationPdp pdp;
  final PathResolver resolver;

  public FineGrainedAuthzVoter(PathResolver resolver, PathAuthorizationPdp pdp) {
    this.resolver = resolver;
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
      LOG.debug("Authorization SUCCESS for request '{}' authorized by policy '{}'",
          requestToString(invocation.getHttpRequest()), result.getPolicy().get());
    }
  }

  public void logAuthorizationFailure(FilterInvocation invocation, PathAuthorizationResult result) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Authorization FAILURE for request '{}'. Policy: '{}'",
          requestToString(invocation.getHttpRequest()), result.getPolicy());
    }
  }


  public StorageAreaInfo resolveStorageArea(HttpServletRequest request) {
    final String requestPath = getRequestPath(request);
    return resolver.resolveStorageArea(requestPath);
  }


  @Override
  public int vote(Authentication authentication, FilterInvocation invocation,
      Collection<ConfigAttribute> attributes) {

    StorageAreaInfo sa = resolveStorageArea(invocation.getRequest());

    if (isNull(sa) || Boolean.FALSE.equals(sa.fineGrainedAuthzEnabled())) {
      return ACCESS_ABSTAIN;
    }

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
