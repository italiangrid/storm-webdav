// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.managers;

import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.PUT;

import java.net.MalformedURLException;
import java.util.function.Supplier;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public class FineGrainedCopyMoveAuthzManager extends PathAuthzPdpManagerSupport {

  public static final Logger LOG = LoggerFactory.getLogger(FineGrainedCopyMoveAuthzManager.class);

  public FineGrainedCopyMoveAuthzManager(ServiceConfigurationProperties config,
      PathResolver resolver, PathAuthorizationPdp pdp, LocalURLService localUrlService) {
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

    if (!isCopyOrMoveRequest(requestAuthorizationContext.getRequest())) {
      return null;
    }

    String destination =
        requestAuthorizationContext.getRequest().getHeader(TransferConstants.DESTINATION_HEADER);

    if (destination == null) {
      return null;
    }

    if (COPY.name().equals(requestAuthorizationContext.getRequest().getMethod())
        && requestHasRemoteDestinationHeader(requestAuthorizationContext.getRequest(),
            localUrlService)) {
      return null;
    }

    try {

      String destinationPath = getSanitizedPathFromUrl(destination);
      StorageAreaInfo sa = resolver.resolveStorageArea(destinationPath);

      if (sa == null) {
        return null;
      }

      if (!sa.fineGrainedAuthzEnabled()) {
        return null;
      }

      return renderDecision(
          PathAuthorizationRequest.newAuthorizationRequest(requestAuthorizationContext.getRequest(),
              authentication.get(), destinationPath, PUT),
          LOG);

    } catch (MalformedURLException e) {
      return renderDecision(PathAuthorizationResult.deny(e.getMessage()));
    }

  }

}
