// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.oauth.jwt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.oauth.authzserver.DefaultTokenIssuerService.BEARER_TOKEN_TYPE;
import static org.mockito.Mockito.lenient;

import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest;
import org.italiangrid.storm.webdav.oauth.authzserver.DefaultTokenIssuerService;
import org.italiangrid.storm.webdav.oauth.authzserver.TokenResponseDTO;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.SignedJwtTokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

  public static final Instant NOW = Instant.parse("2018-01-01T00:00:00.00Z");
  public static final Instant NOW_PLUS_100_SECS = NOW.plusSeconds(100);

  public static final String JWT_SERIALIZED_FORM = "shagdfhdgj.dsadhgjagsdjad.dasdgdhadjsagdh";

  @Mock
  AuthorizationServerProperties props;

  @Mock
  SignedJwtTokenIssuer tokenIssuer;

  Clock mockClock = Clock.fixed(NOW, ZoneId.systemDefault());

  DefaultTokenIssuerService issuerService;

  @Mock
  SignedJWT jwt;

  @Mock
  AccessTokenRequest request;

  @Mock
  Authentication auth;

  JWTClaimsSet claimsSet =
      new JWTClaimsSet.Builder().expirationTime(Date.from(NOW_PLUS_100_SECS)).build();

  @BeforeEach
  void setup() throws ParseException {

    issuerService = new DefaultTokenIssuerService(props, tokenIssuer, mockClock);

    lenient().when(tokenIssuer.createAccessToken(request, auth)).thenReturn(jwt);
    lenient().when(jwt.serialize()).thenReturn(JWT_SERIALIZED_FORM);
    lenient().when(jwt.getJWTClaimsSet()).thenReturn(claimsSet);
  }

  @Test
  void canGenerateTokenResponse() {
    TokenResponseDTO response = issuerService.createAccessToken(request, auth);

    assertThat(response.getTokenType(), is(BEARER_TOKEN_TYPE));
    assertThat(response.getExpiresIn(), is(100));
    assertThat(response.getAccessToken(), is(JWT_SERIALIZED_FORM));
  }
}
