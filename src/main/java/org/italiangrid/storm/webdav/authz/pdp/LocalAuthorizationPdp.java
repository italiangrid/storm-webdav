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
package org.italiangrid.storm.webdav.authz.pdp;

import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.deny;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.indeterminate;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.permit;
import static org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer.PATH_CLAIM;
import static org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer.PERMS_CLAIM;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.ResourceAccessTokenRequest.Permission;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class LocalAuthorizationPdp implements PathAuthorizationPdp, TpcUtils {
  public static final String INSUFFICIENT_PRIVILEGES = "Insufficient privileges";

  public static final Logger LOG = LoggerFactory.getLogger(LocalAuthorizationPdp.class);

  private static final Set<Permission> READ_PERMS = EnumSet.of(Permission.r, Permission.rw);
  private static final Set<Permission> WRITE_PERMS = EnumSet.of(Permission.w, Permission.rw);

  private final URL localAuthzServerIssuer;

  public LocalAuthorizationPdp(ServiceConfigurationProperties config) throws MalformedURLException {
    localAuthzServerIssuer = new URL(config.getAuthzServer().getIssuer());
  }

  private Supplier<IllegalArgumentException> claimNotFound(String claimName) {
    return () -> new IllegalArgumentException(
        String.format("Invalid token: '%s' claim not found", claimName));
  }

  @Override
  public PathAuthorizationResult authorizeRequest(PathAuthorizationRequest authzRequest) {

    final HttpServletRequest request = authzRequest.getRequest();
    final String path =
        Optional.ofNullable(authzRequest.getPath()).orElse(getSerlvetRequestPath(request));

    final String method = Optional.ofNullable(authzRequest.getMethod()).orElse(request.getMethod());

    JwtAuthenticationToken token = (JwtAuthenticationToken) authzRequest.getAuthentication();

    if (!localAuthzServerIssuer.equals(token.getToken().getIssuer())) {
      return indeterminate();
    }

    final String tokenPath = Optional.ofNullable(token.getToken().getClaimAsString(PATH_CLAIM))
      .orElseThrow(claimNotFound(PATH_CLAIM));

    final Permission perm =
        Permission.valueOf(Optional.ofNullable(token.getToken().getClaimAsString(PERMS_CLAIM))
          .orElseThrow(claimNotFound(PERMS_CLAIM)));

    final boolean pathMatches = path.equals(tokenPath);
    final boolean permMatches = ("GET".equals(method) && READ_PERMS.contains(perm))
        || ("PUT".equals(method) && WRITE_PERMS.contains(perm));

    final Optional<String> originIp =
        Optional.ofNullable(token.getToken().getClaimAsString(DefaultJwtTokenIssuer.ORIGIN_CLAIM));

    boolean originMatches = true;

    if (originIp.isPresent()) {
      originMatches = originIp.get().equals(request.getRemoteAddr());
    }

    final boolean ok = pathMatches && permMatches && originMatches;

    if (LOG.isDebugEnabled()) {
      if (originIp.isPresent()) {
        LOG.debug("Ok: {}, pathMatches: {}, permMatches: {}, originMatches: {}", ok, pathMatches,
            permMatches, originMatches);
      } else {
        LOG.debug("Ok: {}, pathMatches: {}, permMatches: {}", ok, pathMatches, permMatches);
      }
    }

    if (!ok) {
      return deny(INSUFFICIENT_PRIVILEGES);
    }

    return permit();
  }

}
