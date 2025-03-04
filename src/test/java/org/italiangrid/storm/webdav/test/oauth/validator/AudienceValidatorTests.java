// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.oauth.validator;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;

import org.italiangrid.storm.webdav.config.OAuthProperties.AuthorizationServer;
import org.italiangrid.storm.webdav.oauth.validator.AudienceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AudienceValidatorTests {

  @Mock
  AuthorizationServer server;

  AudienceValidator validator;

  @Mock
  Jwt jwt;

  @BeforeEach
  void setup() {
    lenient().when(server.getAudiences()).thenReturn(List.of("https://storm.example:8443", "any"));

  }

  @Test
  void testNullAudiences() {
    when(server.getAudiences()).thenReturn(null);
    assertThrows(NullPointerException.class, () -> {
      validator = new AudienceValidator(server);
    });
  }

  @Test
  void testEmptyAudiences() {
    when(server.getAudiences()).thenReturn(emptyList());
    assertThrows(IllegalArgumentException.class, () -> {
      validator = new AudienceValidator(server);
    });
  }

  @Test
  void testNoAudienceInTokenYeldsSuccess() {
    when(jwt.getAudience()).thenReturn(null);
    validator = new AudienceValidator(server);
    assertThat(validator.validate(jwt).hasErrors(), is(false));
  }

  @Test
  void testEmptyAudienceInTokenYeldsSuccess() {
    when(jwt.getAudience()).thenReturn(emptyList());
    validator = new AudienceValidator(server);
    assertThat(validator.validate(jwt).hasErrors(), is(false));
  }

  @Test
  void testInvalidAudienceIsError() {
    when(jwt.getAudience()).thenReturn(List.of("testAudience"));
    validator = new AudienceValidator(server);
    assertThat(validator.validate(jwt).hasErrors(), is(true));
  }

  @Test
  void testAudienceValidationSuccess() {
    when(jwt.getAudience()).thenReturn(List.of("any"));
    validator = new AudienceValidator(server);
    assertThat(validator.validate(jwt).hasErrors(), is(false));
  }

}
