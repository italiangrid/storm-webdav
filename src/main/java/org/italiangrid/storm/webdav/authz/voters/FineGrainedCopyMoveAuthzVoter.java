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
package org.italiangrid.storm.webdav.authz.voters;

import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.PUT;

import java.net.MalformedURLException;
import java.util.Collection;

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
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

public class FineGrainedCopyMoveAuthzVoter extends PathAuthzPdpVoterSupport {

  public static final Logger LOG = LoggerFactory.getLogger(FineGrainedCopyMoveAuthzVoter.class);

  public FineGrainedCopyMoveAuthzVoter(ServiceConfigurationProperties config, PathResolver resolver,
      PathAuthorizationPdp pdp, LocalURLService localUrlService) {
    super(config, resolver, pdp, localUrlService, true);
  }

  @Override
  public int vote(Authentication authentication, FilterInvocation filter,
      Collection<ConfigAttribute> attributes) {

    if (!isCopyOrMoveRequest(filter.getRequest())) {
      return ACCESS_ABSTAIN;
    }

    String destination = filter.getRequest().getHeader(TransferConstants.DESTINATION_HEADER);

    if (destination == null) {
      return ACCESS_ABSTAIN;
    }

    if (COPY.name().equals(filter.getRequest().getMethod())
        && requestHasRemoteDestinationHeader(filter.getRequest(), localUrlService)) {
      return ACCESS_ABSTAIN;
    }

    try {

      String destinationPath = getSanitizedPathFromUrl(destination);
      StorageAreaInfo sa = resolver.resolveStorageArea(destinationPath);

      if (sa == null) {
        return ACCESS_ABSTAIN;
      }

      if (!sa.fineGrainedAuthzEnabled()) {
        return ACCESS_ABSTAIN;
      }

      return renderDecision(PathAuthorizationRequest
        .newAuthorizationRequest(filter.getHttpRequest(), authentication, destinationPath, PUT),
          LOG);

    } catch (MalformedURLException e) {
      return renderDecision(PathAuthorizationResult.deny(e.getMessage()));
    }

  }

}
