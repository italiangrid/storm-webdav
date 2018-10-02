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
package org.italiangrid.storm.webdav.oauth.authzserver;

import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.SignedJwtTokenIssuer;
import org.springframework.security.core.Authentication;

import com.nimbusds.jwt.SignedJWT;

public class DefaultTokenIssuerService implements TokenIssuerService {

  public static final String BEARER_TOKEN_TYPE = "Bearer";

  final SignedJwtTokenIssuer tokenIssuer;
  final AuthorizationServerProperties props;
  final Clock clock;

  public DefaultTokenIssuerService(AuthorizationServerProperties props,
      SignedJwtTokenIssuer tokenIssuer, Clock clock) {
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
  public TokenResponseDTO createAccessToken(AccessTokenRequest tokenRequest,
      Authentication authentication) {

    SignedJWT jwt = tokenIssuer.createAccessToken(tokenRequest, authentication);

    TokenResponseDTO response = new TokenResponseDTO();

    response.setExpiresIn(getTokenValidityInSeconds(jwt));
    response.setTokenType(BEARER_TOKEN_TYPE);
    response.setAccessToken(jwt.serialize());

    return response;
  }

}
