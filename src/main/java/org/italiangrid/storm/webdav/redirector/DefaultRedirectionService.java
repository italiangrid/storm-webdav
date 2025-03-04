// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.redirector;

import java.net.URI;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.RedirectorProperties.ReplicaEndpointProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.ResourceAccessTokenRequest;
import org.italiangrid.storm.webdav.oauth.authzserver.ResourceAccessTokenRequest.Permission;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.SignedJwtTokenIssuer;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.nimbusds.jwt.SignedJWT;

@Service
@ConditionalOnProperty(name = "storm.redirector.enabled", havingValue = "true")
public class DefaultRedirectionService implements RedirectionService, TpcUtils {

  public static final Logger LOG = LoggerFactory.getLogger(DefaultRedirectionService.class);

  public static final String ACCESS_TOKEN_QUERY_PARAM = "access_token";

  private final ServiceConfigurationProperties config;

  private final SignedJwtTokenIssuer tokenIssuer;
  private final ReplicaSelector selector;

  @Autowired
  public DefaultRedirectionService(ServiceConfigurationProperties config,
      SignedJwtTokenIssuer tokenIssuer, ReplicaSelector selector) {
    this.config = config;
    this.tokenIssuer = tokenIssuer;
    this.selector = selector;
  }

  private Supplier<RedirectError> noReplicaFound() {
    return () -> new RedirectError("No replica found for current replica selection policy");
  }

  @Override
  public String buildRedirect(Authentication authentication, HttpServletRequest request,
      HttpServletResponse response) {
    ReplicaEndpointProperties replica = selector.selectReplica().orElseThrow(noReplicaFound());
    URI endpointUri = replica.getEndpoint();
    LOG.debug("Selected endpoint: {}", endpointUri);

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(endpointUri);

    String path = getSerlvetRequestPath(request);
    Permission perm = Permission.r;

    if ("PUT".equals(request.getMethod())) {
      perm = Permission.rw;
    }

    SignedJWT token = tokenIssuer.createResourceAccessToken(ResourceAccessTokenRequest.forPath(path,
        perm, config.getRedirector().getMaxTokenLifetimeSecs(), request.getRemoteAddr()),
        authentication);

    uriBuilder.path(path);
    uriBuilder.queryParam(ACCESS_TOKEN_QUERY_PARAM, token.serialize());

    String url = uriBuilder.toUriString();

    LOG.debug("Built redirect URL: {}", url);

    return url;
  }

}
