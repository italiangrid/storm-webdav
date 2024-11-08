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

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.authz.util.MatcherUtils;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.slf4j.Logger;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public abstract class PathAuthzPdpManagerSupport
    implements MatcherUtils, TpcUtils, AuthorizationManager<RequestAuthorizationContext> {

  protected static final Set<PathAuthorizationResult.Decision> ABSTAIN_DECISIONS =
      EnumSet.of(PathAuthorizationResult.Decision.INDETERMINATE,
          PathAuthorizationResult.Decision.NOT_APPLICABLE);

  protected final ServiceConfigurationProperties config;
  protected final PathResolver resolver;
  protected final PathAuthorizationPdp pdp;
  protected final LocalURLService localUrlService;
  protected final boolean permissive;

  protected PathAuthzPdpManagerSupport(ServiceConfigurationProperties config, PathResolver resolver,
      PathAuthorizationPdp pdp, LocalURLService localUrlService, boolean permissive) {
    this.config = config;
    this.resolver = resolver;
    this.pdp = pdp;
    this.localUrlService = localUrlService;
    this.permissive = permissive;
  }

  protected void logPdpDecision(PathAuthorizationRequest request, PathAuthorizationResult result,
      Logger logger) {
    String requestString = requestToString(request);
    logger.debug("Request: {}. Path: {}. Decision: {}. message: {}. Policy: {}", requestString,
        Optional.ofNullable(request.getPath()), result.getDecision(), result.getMessage(),
        result.getPolicy());
  }

  public AuthorizationDecision renderDecision(PathAuthorizationResult result) {
    if (ABSTAIN_DECISIONS.contains(result.getDecision()) && permissive) {
      return null;
    }

    if (PathAuthorizationResult.Decision.PERMIT.equals(result.getDecision())) {
      return new AuthorizationDecision(true);
    }

    return new AuthorizationDecision(false);
  }

  public AuthorizationDecision renderDecision(PathAuthorizationRequest request, Logger log) {
    PathAuthorizationResult result = pdp.authorizeRequest(request);

    logPdpDecision(request, result, log);

    return renderDecision(result);
  }

  public boolean isPermissive() {
    return permissive;
  }
}
