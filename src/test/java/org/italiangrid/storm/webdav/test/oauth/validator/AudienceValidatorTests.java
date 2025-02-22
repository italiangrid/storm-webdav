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
