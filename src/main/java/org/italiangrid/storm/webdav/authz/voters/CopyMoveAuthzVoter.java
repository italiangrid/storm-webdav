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
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.regex.Pattern;

import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

public class CopyMoveAuthzVoter implements AccessDecisionVoter<FilterInvocation>, TpcUtils {

  protected static final Logger logger = LoggerFactory.getLogger(CopyMoveAuthzVoter.class);

  protected static final String DESTINATION = "Destination";

  protected static final String WEBDAV_PATH_REGEX = "/webdav/(.*)$";
  protected static final Pattern WEBDAV_PATH_PATTERN = Pattern.compile(WEBDAV_PATH_REGEX);

  protected final StorageAreaConfiguration saConfig;
  protected final PathResolver pathResolver;
  protected final LocalURLService localURLservice;

  public CopyMoveAuthzVoter(StorageAreaConfiguration saConfig, PathResolver pathResolver,
      LocalURLService lus) {

    this.saConfig = saConfig;
    this.pathResolver = pathResolver;
    this.localURLservice = lus;
  }

  @Override
  public boolean supports(ConfigAttribute attribute) {

    return false;
  }

  @Override
  public boolean supports(Class<?> clazz) {

    return FilterInvocation.class.isAssignableFrom(clazz);

  }


  @Override
  public int vote(Authentication authentication, FilterInvocation filter,
      Collection<ConfigAttribute> attributes) {

    if (!isCopyOrMoveRequest(filter.getRequest())) {
      return ACCESS_ABSTAIN;
    }

    String destination = filter.getRequest().getHeader(DESTINATION);
    if (destination == null) {
      return ACCESS_ABSTAIN;
    }

    if (COPY.name().equals(filter.getRequest().getMethod())
        && requestHasRemoteDestinationHeader(filter.getRequest(), localURLservice)) {
      return ACCESS_ABSTAIN;
    }

    try {

      String destinationPath = getSanitizedPathFromUrl(destination);
      StorageAreaInfo sa = pathResolver.resolveStorageArea(destinationPath);

      if (isNull(sa)) {
        return ACCESS_DENIED;
      }

      if (Boolean.TRUE.equals(sa.fineGrainedAuthzEnabled())) {
        return ACCESS_ABSTAIN;
      }

      if (authentication.getAuthorities().contains(SAPermission.canWrite(sa.name()))) {
        return ACCESS_GRANTED;
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Access denied. PrincipalProperties does not have write permissions on "
            + "storage area {}", sa.name());
      }

      return ACCESS_DENIED;

    } catch (MalformedURLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }

  }

}
