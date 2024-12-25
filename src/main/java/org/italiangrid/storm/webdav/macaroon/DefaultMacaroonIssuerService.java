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
package org.italiangrid.storm.webdav.macaroon;

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

import com.nimbusds.jwt.SignedJWT;

@Component
@ConditionalOnExpression("${storm.macaroon-filter.enabled:false} && ${storm.authz-server.enabled:false}")
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

    if (StringUtils.hasText(request.getValidity())) {

      try {
        final long requestedValidity = Duration.parse(request.getValidity()).getSeconds();
        req.setLifetime(requestedValidity);
      } catch (DateTimeParseException e) {
        LOG.warn("Invalid validity string: {}", request.getValidity());
      }
    }
    return req;
  }

  @Override
  @PreAuthorize("#storm.isVOMSAuthenticated()")
  public MacaroonResponseDTO createAccessToken(MacaroonRequestDTO request, Authentication auth) {

    SignedJWT jwt = tokenIssuer.createAccessToken(createRequest(request), auth);
    MacaroonResponseDTO response = new MacaroonResponseDTO();
    response.setMacaroon(jwt.serialize());

    return response;
  }

}
