// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public class FineGrainedAuthzManager extends PathAuthzPdpManagerSupport {

  public static final Logger LOG = LoggerFactory.getLogger(FineGrainedAuthzManager.class);

  public FineGrainedAuthzManager(
      ServiceConfigurationProperties config,
      PathResolver resolver,
      PathAuthorizationPdp pdp,
      LocalURLService localUrlService) {
    super(config, resolver, pdp, localUrlService, true);
  }

  @Override
  public AuthorizationResult authorize(
      Supplier<? extends Authentication> authentication,
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
