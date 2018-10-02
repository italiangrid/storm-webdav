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
package org.italiangrid.storm.webdav.oauth.authzserver.jwt;

import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.italiangrid.storm.webdav.authz.AuthorizationPolicyService;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationDetails;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest;
import org.italiangrid.storm.webdav.oauth.authzserver.TokenCreationError;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class DefaultJwtTokenIssuer implements SignedJwtTokenIssuer {

  public static final JWSAlgorithm JWS_ALGO = JWSAlgorithm.HS256;
  public static final String CLAIM_AUTHORITIES = "authorities";

  final Clock clock;

  final AuthorizationServerProperties properties;
  final AuthorizationPolicyService policyService;
  final JWSSigner signer;


  public DefaultJwtTokenIssuer(Clock clock, AuthorizationServerProperties props,
      AuthorizationPolicyService ps) {

    this.clock = clock;
    this.properties = props;
    this.policyService = ps;

    try {
      signer = new MACSigner(properties.getSecret());
    } catch (KeyLengthException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected Optional<Instant> vomsAcExpiration(Authentication authentication) {

    if (authentication.getDetails() instanceof VOMSAuthenticationDetails) {
      VOMSAuthenticationDetails details = (VOMSAuthenticationDetails) authentication.getDetails();

      if (!details.getVomsAttributes().isEmpty()) {
        Date acNotAfter = details.getVomsAttributes().get(0).getNotAfter();
        return Optional.of(acNotAfter.toInstant());
      }
    }

    return Optional.empty();
  }

  protected Date computeTokenExpirationTimestamp(Authentication authentication) {

    Instant defaultExpiration = clock.instant().plusSeconds(properties.getMaxTokenLifetimeSec());
    Instant expiration = defaultExpiration;

    Optional<Instant> acExpiration = vomsAcExpiration(authentication);

    if (acExpiration.isPresent()) {
      if (acExpiration.get().isBefore(defaultExpiration)) {
        expiration = acExpiration.get();
      }
    }
    
    return Date.from(expiration);
  }

  @Override
  public SignedJWT createAccessToken(AccessTokenRequest request, Authentication authentication) {

    Set<GrantedAuthority> authorities = policyService.getSAPermissions(authentication);
    JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();

    claimsSet.issuer(properties.getIssuer());
    claimsSet.audience(properties.getIssuer());
    claimsSet.subject(authentication.getName());
    claimsSet.expirationTime(computeTokenExpirationTimestamp(authentication));
    claimsSet.claim(CLAIM_AUTHORITIES,
        authorities.stream().map(Object::toString).collect(toList()));

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWS_ALGO), claimsSet.build());

    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new TokenCreationError(e);
    }
    return signedJWT;
  }

}
