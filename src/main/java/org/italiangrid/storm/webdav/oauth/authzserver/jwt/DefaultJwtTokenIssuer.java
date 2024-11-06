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
package org.italiangrid.storm.webdav.oauth.authzserver.jwt;

import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.italiangrid.storm.webdav.authn.PrincipalHelper;
import org.italiangrid.storm.webdav.authz.AuthorizationPolicyService;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationDetails;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest;
import org.italiangrid.storm.webdav.oauth.authzserver.ResourceAccessTokenRequest;
import org.italiangrid.storm.webdav.oauth.authzserver.TokenCreationError;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Sets;
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
  public static final String PATH_CLAIM = "path";
  public static final String ORIGIN_CLAIM = "origin";
  public static final String PERMS_CLAIM = "perms";

  final Clock clock;

  final AuthorizationServerProperties properties;
  final AuthorizationPolicyService policyService;
  final JWSSigner signer;
  final PrincipalHelper helper;


  public DefaultJwtTokenIssuer(Clock clock, AuthorizationServerProperties props,
      AuthorizationPolicyService ps, PrincipalHelper principalHelper) {

    this.clock = clock;
    this.properties = props;
    this.policyService = ps;
    this.helper = principalHelper;

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

  protected Date computeTokenExpirationTimestamp(AccessTokenRequest request,
      Authentication authentication) {

    Optional<Instant> requestedExpiration = Optional.empty();

    Instant now = clock.instant();
    Instant defaultExpiration = now.plusSeconds(properties.getMaxTokenLifetimeSec());
    Instant expiration = defaultExpiration;

    if (request.getLifetime() != null && request.getLifetime() > 0) {
      requestedExpiration = Optional.of(now.plusSeconds(request.getLifetime()));
    }

    if (requestedExpiration.isPresent() && requestedExpiration.get().isBefore(expiration)) {
      expiration = requestedExpiration.get();
    }

    Optional<Instant> acExpiration = vomsAcExpiration(authentication);

    if (acExpiration.isPresent() && acExpiration.get().isBefore(expiration)) {
      expiration = acExpiration.get();
    }

    return Date.from(expiration);
  }


  protected Date computeResourceTokenExpiration(ResourceAccessTokenRequest request) {
    Instant now = clock.instant();
    Instant defaultExpiration = now.plusSeconds(request.getLifetimeSecs());
    return Date.from(defaultExpiration);
  }

  @Override
  public SignedJWT createAccessToken(AccessTokenRequest request, Authentication authentication) {

    Set<GrantedAuthority> tokenAuthorities = Sets.newHashSet();

    Set<GrantedAuthority> saAuthorities = policyService.getSAPermissions(authentication);
    tokenAuthorities.addAll(saAuthorities);

    tokenAuthorities.addAll(authentication.getAuthorities());

    JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();

    claimsSet.issuer(properties.getIssuer());
    claimsSet.audience(properties.getIssuer());
    claimsSet.subject(authentication.getName());
    claimsSet.expirationTime(computeTokenExpirationTimestamp(request, authentication));

    claimsSet.claim(CLAIM_AUTHORITIES,
        tokenAuthorities.stream().map(Object::toString).collect(toList()));

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWS_ALGO), claimsSet.build());

    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new TokenCreationError(e);
    }
    return signedJWT;
  }

  @Override
  public SignedJWT createResourceAccessToken(ResourceAccessTokenRequest request,
      Authentication authentication) {

    JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();
    claimsSet.issuer(properties.getIssuer());
    claimsSet.audience(properties.getIssuer());

    claimsSet.subject(helper.getPrincipalAsString(authentication));
    claimsSet.expirationTime(computeResourceTokenExpiration(request));

    claimsSet.claim(PATH_CLAIM, request.getPath());
    claimsSet.claim(PERMS_CLAIM, request.getPermission().name());
    claimsSet.claim(ORIGIN_CLAIM, request.getOrigin());

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWS_ALGO), claimsSet.build());

    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new TokenCreationError(e);
    }
    return signedJWT;
  }

}
