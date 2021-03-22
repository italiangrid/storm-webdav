/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.test.oauth.jwt;

import static org.hamcrest.CoreMatchers.is;
import static org.italiangrid.storm.webdav.oauth.authzserver.DefaultTokenIssuerService.BEARER_TOKEN_TYPE;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@RunWith(MockitoJUnitRunner.class)
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

  @Before
  public void setup() throws ParseException {

    issuerService = new DefaultTokenIssuerService(props, tokenIssuer, mockClock);
    
    when(tokenIssuer.createAccessToken(request, auth)).thenReturn(jwt);
    when(jwt.serialize()).thenReturn(JWT_SERIALIZED_FORM);
    when(jwt.getJWTClaimsSet()).thenReturn(claimsSet);
  }

  @Test
  public void canGenerateTokenResponse() {
    TokenResponseDTO response = issuerService.createAccessToken(request, auth);

    assertThat(response.getTokenType(), is(BEARER_TOKEN_TYPE));
    assertThat(response.getExpiresIn(), is(100));
    assertThat(response.getAccessToken(), is(JWT_SERIALIZED_FORM));
  }
}
