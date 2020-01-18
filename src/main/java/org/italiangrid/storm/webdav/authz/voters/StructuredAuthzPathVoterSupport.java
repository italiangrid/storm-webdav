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

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.authz.util.MatcherUtils;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;

public abstract class StructuredAuthzPathVoterSupport
    implements MatcherUtils, TpcUtils, AccessDecisionVoter<FilterInvocation> {
  public static final Logger LOG = LoggerFactory.getLogger(StructuredAuthzPathVoterSupport.class);

  public static final EnumSet<PathAuthorizationResult.Decision> ABSTAIN_DECISIONS =
      EnumSet.of(PathAuthorizationResult.Decision.INDETERMINATE,
          PathAuthorizationResult.Decision.NOT_APPLICABLE);

  protected final ServiceConfigurationProperties config;
  protected final PathResolver resolver;
  protected final PathAuthorizationPdp pdp;

  public StructuredAuthzPathVoterSupport(ServiceConfigurationProperties config,
      PathResolver resolver, PathAuthorizationPdp pdp) {
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

  protected void abstainlogPdpMessage(String m) {
    LOG.debug("Abstained. Pdp message: {}", m);
  }

  protected void denylogPdpMessage(String m) {
    LOG.debug("Access denied. Pdp message: {}", m);
  }
  
  public int renderDecision(PathAuthorizationRequest request) {
    PathAuthorizationResult result = pdp.authorizeRequest(request);

    if (ABSTAIN_DECISIONS.contains(result.getDecision())) {
      result.getMessage().ifPresent(this::abstainlogPdpMessage);
      return ACCESS_ABSTAIN;
    }

    if (PathAuthorizationResult.Decision.PERMIT.equals(result.getDecision())) {
      return ACCESS_GRANTED;
    }

    result.getMessage().ifPresent(this::denylogPdpMessage);
    return ACCESS_DENIED;
  }
}
