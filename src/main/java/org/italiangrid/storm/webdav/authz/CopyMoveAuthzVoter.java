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
package org.italiangrid.storm.webdav.authz;

import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

public class CopyMoveAuthzVoter implements AccessDecisionVoter<FilterInvocation> {

  private static final Logger logger = LoggerFactory.getLogger(CopyMoveAuthzVoter.class);

  private static final String DESTINATION = "Destination";

  private static final String WEBDAV_PATH_REGEX = "/webdav/(.*)$";
  private static final Pattern WEBDAV_PATH_PATTERN = Pattern.compile(WEBDAV_PATH_REGEX);

  final StorageAreaConfiguration saConfig;
  final PathResolver pathResolver;
  final LocalURLService localURLservice;

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

  private boolean isCopyOrMoveRequest(HttpServletRequest req) {

    final String method = req.getMethod();
    return (method.equals("COPY") || method.equals("MOVE"));

  }

  private String dropSlashWebdavFromPath(String path) {
    Matcher m = WEBDAV_PATH_PATTERN.matcher(path);

    if (m.matches()) {
      return String.format("/%s", m.group(1));
    }

    return path;
  }

  private StorageAreaInfo getSAFromPath(String destinationURL) throws MalformedURLException {

    URL url = new URL(destinationURL);
    String pathInContext = dropSlashWebdavFromPath(url.getPath());
    return pathResolver.resolveStorageArea(pathInContext);
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
        && !localURLservice.isLocalURL(destination)) {
      return ACCESS_ABSTAIN;
    }

    try {

      StorageAreaInfo sa = getSAFromPath(destination);

      if (sa == null) {
        return ACCESS_DENIED;
      }

      if (authentication.getAuthorities().contains(SAPermission.canWrite(sa.name()))) {

        return ACCESS_GRANTED;
      }

      if (logger.isDebugEnabled()) {
        logger.debug(
            "Access denied. Principal does not have write permissions on " + "storage area {}",
            sa.name());

      }

      return ACCESS_DENIED;

    } catch (MalformedURLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }

  }

}
