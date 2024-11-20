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

import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;

import java.util.function.Supplier;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;


public class FineGrainedAuthzManager extends PathAuthzPdpManagerSupport {

  public static final Logger LOG = LoggerFactory.getLogger(FineGrainedAuthzManager.class);

  public FineGrainedAuthzManager(ServiceConfigurationProperties config, PathResolver resolver,
      PathAuthorizationPdp pdp, LocalURLService localUrlService) {
    super(config, resolver, pdp, localUrlService, true);
  }

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

    final String requestPath = getRequestPath(requestAuthorizationContext.getRequest());
    StorageAreaInfo sa = resolver.resolveStorageArea(requestPath);

    if (sa == null || !sa.fineGrainedAuthzEnabled()) {
      return null;
    }

    return renderDecision(
        newAuthorizationRequest(requestAuthorizationContext.getRequest(), authentication.get()),
        LOG);

  }

}
