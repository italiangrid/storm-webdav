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

import java.util.EnumSet;
import java.util.Set;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.authz.util.MatcherUtils;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;

public abstract class PathAuthzPdpVoterSupport
    implements MatcherUtils, TpcUtils, AccessDecisionVoter<FilterInvocation> {

  protected static final Set<PathAuthorizationResult.Decision> ABSTAIN_DECISIONS =
      EnumSet.of(PathAuthorizationResult.Decision.INDETERMINATE,
          PathAuthorizationResult.Decision.NOT_APPLICABLE);

  protected final ServiceConfigurationProperties config;
  protected final PathResolver resolver;
  protected final PathAuthorizationPdp pdp;

  public PathAuthzPdpVoterSupport(ServiceConfigurationProperties config, PathResolver resolver,
      PathAuthorizationPdp pdp) {
    this.config = config;
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

  public String getStorageAreaPath(String requestPath, StorageAreaInfo sa) {
    return sa.accessPoints()
      .stream()
      .filter(requestPath::startsWith)
      .findFirst()
      .map(s -> requestPath.substring(s.length()))
      .filter(s -> !s.isEmpty())
      .orElse("/");
  }

  protected void abstainlogPdpMessage(PathAuthorizationRequest request, Logger logger, String m) {
    logger.debug("Abstained. Request: {}. Pdp message: {}", request, m);
  }

  protected void denylogPdpMessage(PathAuthorizationRequest request, Logger logger, String m) {
    logger.debug("Access denied. Request: {}. Pdp message: {}", request, m);
  }

  public int renderDecision(PathAuthorizationRequest request, Logger log) {
    PathAuthorizationResult result = pdp.authorizeRequest(request);

    if (ABSTAIN_DECISIONS.contains(result.getDecision())) {
      result.getMessage().ifPresent(m -> abstainlogPdpMessage(request, log, m));
      return ACCESS_ABSTAIN;
    }

    if (PathAuthorizationResult.Decision.PERMIT.equals(result.getDecision())) {
      return ACCESS_GRANTED;
    }

    result.getMessage().ifPresent(m -> denylogPdpMessage(request, log, m));
    return ACCESS_DENIED;
  }
}
