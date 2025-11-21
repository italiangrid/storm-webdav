// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.macaroon;

import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.SignedJwtTokenIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnExpression(
    "${storm.macaroon-filter.enabled:false} && ${storm.authz-server.enabled:false}")
public class DefaultMacaroonIssuerService implements MacaroonIssuerService {

  public static final Logger LOG = LoggerFactory.getLogger(DefaultMacaroonIssuerService.class);

  final SignedJwtTokenIssuer tokenIssuer;
  final Clock clock;

  public DefaultMacaroonIssuerService(SignedJwtTokenIssuer tokenIssuer, Clock clock) {
    this.tokenIssuer = tokenIssuer;
    this.clock = clock;
  }

  protected AccessTokenRequest createRequest(MacaroonRequestDTO request) {
    AccessTokenRequest req = new AccessTokenRequest();

    if (StringUtils.hasText(request.validity())) {

      try {
        final long requestedValidity = Duration.parse(request.validity()).getSeconds();
        req.setLifetime(requestedValidity);
      } catch (DateTimeParseException e) {
        LOG.warn("Invalid validity string: {}", request.validity());
      }
    }
    return req;
  }

  @Override
  @PreAuthorize("#storm.isVOMSAuthenticated()")
  public MacaroonResponseDTO createAccessToken(MacaroonRequestDTO request, Authentication auth) {

    SignedJWT jwt = tokenIssuer.createAccessToken(createRequest(request), auth);
    return new MacaroonResponseDTO(jwt.serialize());
  }
}
