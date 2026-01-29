// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.oauth.validator;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.italiangrid.storm.webdav.config.OAuthProperties.AuthorizationServer;
import org.italiangrid.storm.webdav.oauth.validator.TypAwareValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JoseHeaderNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwtValidators.AtJwtBuilder;

@ExtendWith(MockitoExtension.class)
class TypAwareValidatorTests {

  private static final String ISSUER = "https://wlcg.cloud.cnaf.infn.it/";

  @Mock AuthorizationServer server;

  @Mock OAuth2TokenValidator<Jwt> jwtValidator;

  @Mock OAuth2TokenValidator<Jwt> atJwtValidator;

  @Mock AtJwtBuilder atJwtBuilder;

  TypAwareValidator validator;

  @Mock Jwt jwt;

  @BeforeEach
  void setup() {
    lenient().when(server.issuer()).thenReturn(ISSUER);
    lenient().when(server.audiences()).thenReturn(List.of("https://storm.example:8443", "any"));
    lenient().when(atJwtBuilder.issuer(ISSUER)).thenReturn(atJwtBuilder);
    lenient().when(atJwtBuilder.validators(any())).thenReturn(atJwtBuilder);
    lenient().when(atJwtBuilder.build()).thenReturn(atJwtValidator);
  }

  @Test
  void testWithoutAudiencesJwtValidatorIsAlwaysUsed() {
    when(server.audiences()).thenReturn(List.of());
    try (MockedStatic<JwtValidators> jwtValidators = Mockito.mockStatic(JwtValidators.class)) {
      jwtValidators
          .when(() -> JwtValidators.createDefaultWithIssuer(ISSUER))
          .thenReturn(jwtValidator);
      validator = new TypAwareValidator(server);
      validator.validate(jwt);
      jwtValidators.verify(JwtValidators::createAtJwtValidator, never());
      verify(jwt, times(0)).getHeaders();
      verify(jwtValidator).validate(jwt);
    }
  }

  @Test
  void testJwtValidatorIsUsedWhenTypUnspecified() {
    when(jwt.getHeaders()).thenReturn(Map.of());
    try (MockedStatic<JwtValidators> jwtValidators = Mockito.mockStatic(JwtValidators.class)) {
      jwtValidators
          .when(() -> JwtValidators.createDefaultWithIssuer(ISSUER))
          .thenReturn(jwtValidator);
      jwtValidators.when(JwtValidators::createAtJwtValidator).thenReturn(atJwtBuilder);
      validator = new TypAwareValidator(server);
      validator.validate(jwt);
      verify(jwt, times(1)).getHeaders();
      verify(jwtValidator).validate(jwt);
    }
  }

  @Test
  void testJwtValidatorIsUsedWhenTypJwt() {
    when(jwt.getHeaders()).thenReturn(Map.of(JoseHeaderNames.TYP, "JWT"));
    try (MockedStatic<JwtValidators> jwtValidators = Mockito.mockStatic(JwtValidators.class)) {
      jwtValidators
          .when(() -> JwtValidators.createDefaultWithIssuer(ISSUER))
          .thenReturn(jwtValidator);
      jwtValidators.when(JwtValidators::createAtJwtValidator).thenReturn(atJwtBuilder);
      validator = new TypAwareValidator(server);
      validator.validate(jwt);
      verify(jwt, times(1)).getHeaders();
      verify(jwtValidator).validate(jwt);
    }
  }

  @Test
  void testAtJwtValidatorIsUsedWhenTypAtJwt() {
    when(jwt.getHeaders()).thenReturn(Map.of(JoseHeaderNames.TYP, "at+jwt"));
    try (MockedStatic<JwtValidators> jwtValidators = Mockito.mockStatic(JwtValidators.class)) {
      jwtValidators
          .when(() -> JwtValidators.createDefaultWithIssuer(ISSUER))
          .thenReturn(jwtValidator);
      jwtValidators.when(JwtValidators::createAtJwtValidator).thenReturn(atJwtBuilder);
      validator = new TypAwareValidator(server);
      validator.validate(jwt);
      verify(jwt, times(1)).getHeaders();
      verify(atJwtValidator).validate(jwt);
      verify(jwtValidator, times(0)).validate(jwt);
    }
  }

  @Test
  void testAtJwtValidatorIsUsedWhenTypApplicationAtJwt() {
    when(jwt.getHeaders()).thenReturn(Map.of(JoseHeaderNames.TYP, "application/at+jwt"));
    try (MockedStatic<JwtValidators> jwtValidators = Mockito.mockStatic(JwtValidators.class)) {
      jwtValidators
          .when(() -> JwtValidators.createDefaultWithIssuer(ISSUER))
          .thenReturn(jwtValidator);
      jwtValidators.when(JwtValidators::createAtJwtValidator).thenReturn(atJwtBuilder);
      validator = new TypAwareValidator(server);
      validator.validate(jwt);
      verify(jwt, times(1)).getHeaders();
      verify(atJwtValidator).validate(jwt);
      verify(jwtValidator, times(0)).validate(jwt);
    }
  }
}
