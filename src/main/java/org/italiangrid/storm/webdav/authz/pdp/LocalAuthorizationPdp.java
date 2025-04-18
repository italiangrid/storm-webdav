// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.pdp;

import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.deny;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.indeterminate;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.permit;
import static org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer.PATH_CLAIM;
import static org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer.PERMS_CLAIM;

import jakarta.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.ResourceAccessTokenRequest.Permission;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import io.opentelemetry.instrumentation.annotations.WithSpan;

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
    return () ->
        new IllegalArgumentException(
            String.format("Invalid token: '%s' claim not found", claimName));
  }

  @WithSpan
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

    final String tokenPath =
        Optional.ofNullable(token.getToken().getClaimAsString(PATH_CLAIM))
            .orElseThrow(claimNotFound(PATH_CLAIM));

    final Permission perm =
        Permission.valueOf(
            Optional.ofNullable(token.getToken().getClaimAsString(PERMS_CLAIM))
                .orElseThrow(claimNotFound(PERMS_CLAIM)));

    final boolean pathMatches = path.equals(tokenPath);
    final boolean permMatches =
        ("GET".equals(method) && READ_PERMS.contains(perm))
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
        LOG.debug(
            "Ok: {}, pathMatches: {}, permMatches: {}, originMatches: {}",
            ok,
            pathMatches,
            permMatches,
            originMatches);
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
