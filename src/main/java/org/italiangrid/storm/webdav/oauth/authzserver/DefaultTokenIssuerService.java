// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver;

import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.SignedJwtTokenIssuer;
import org.springframework.security.core.Authentication;

public class DefaultTokenIssuerService implements TokenIssuerService {

  public static final String BEARER_TOKEN_TYPE = "Bearer";

  final SignedJwtTokenIssuer tokenIssuer;
  final AuthorizationServerProperties props;
  final Clock clock;

  public DefaultTokenIssuerService(
      AuthorizationServerProperties props, SignedJwtTokenIssuer tokenIssuer, Clock clock) {
    this.props = props;
    this.tokenIssuer = tokenIssuer;
    this.clock = clock;
  }

  protected int getTokenValidityInSeconds(SignedJWT jwt) {
    try {

      Instant expirationTime = jwt.getJWTClaimsSet().getExpirationTime().toInstant();
      Instant now = clock.instant();
      Duration tokenValidity = Duration.between(now, expirationTime);
      return (int) tokenValidity.getSeconds();

    } catch (ParseException e) {
      throw new IllegalStateException("Error parsing generated token validity", e);
    }
  }

  @Override
  public TokenResponseDTO createAccessToken(
      AccessTokenRequest tokenRequest, Authentication authentication) {

    SignedJWT jwt = tokenIssuer.createAccessToken(tokenRequest, authentication);

    TokenResponseDTO response = new TokenResponseDTO();

    response.setExpiresIn(getTokenValidityInSeconds(jwt));
    response.setTokenType(BEARER_TOKEN_TYPE);
    response.setAccessToken(jwt.serialize());

    return response;
  }
}
