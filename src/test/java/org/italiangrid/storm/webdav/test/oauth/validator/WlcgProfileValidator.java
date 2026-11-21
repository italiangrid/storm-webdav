// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.oauth.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import org.italiangrid.storm.webdav.oauth.validator.WlcgProfileValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class WlcgProfileValidatorTests {

  @Mock Jwt jwt;
  public WlcgProfileValidator validator;

  private void testError(String errorDescription) {
    OAuth2TokenValidatorResult result = validator.validate(jwt);
    assertTrue(result.hasErrors());
    OAuth2Error error = (OAuth2Error) result.getErrors().toArray()[0];
    assertEquals(WlcgProfileValidator.INVALID_TOKEN_ERROR_CODE, error.getErrorCode());
    assertEquals(errorDescription, error.getDescription());
  }

  @Test
  void testValidate() {
    when(jwt.getClaimAsString(WlcgProfileValidator.WLCG_VER_CLAIM)).thenReturn(null);
    final OAuth2TokenValidatorResult success = OAuth2TokenValidatorResult.success();
    validator = new WlcgProfileValidator();
    assertEquals(success, validator.validate(jwt));

    when(jwt.getClaimAsString(WlcgProfileValidator.WLCG_VER_CLAIM)).thenReturn("2.0");
    testError("Unsupported WLCG token profile version");

    when(jwt.getClaimAsString(WlcgProfileValidator.WLCG_VER_CLAIM)).thenReturn("1.0");
    when(jwt.hasClaim(WlcgProfileValidator.SCOPE_CLAIM)).thenReturn(false);
    testError("scope claim not found in token");

    when(jwt.hasClaim(WlcgProfileValidator.SCOPE_CLAIM)).thenReturn(true);
    when(jwt.getNotBefore()).thenReturn(null);
    testError("nbf claim not found in token");

    when(jwt.getNotBefore()).thenReturn(Instant.now());
    when(jwt.getExpiresAt()).thenReturn(null);
    testError("exp claim not found in token");

    when(jwt.getExpiresAt()).thenReturn(Instant.now());
    when(jwt.getSubject()).thenReturn(null);
    testError("sub claim not found in token");

    when(jwt.getSubject()).thenReturn("1234567890");
    when(jwt.getAudience()).thenReturn(null);
    testError("aud claim not found in token");

    when(jwt.getAudience()).thenReturn(new ArrayList<String>());
    testError("aud claim not found in token");

    ArrayList<String> audience = new ArrayList<>();
    audience.add("https://api.example.com");
    audience.add("https://service.example.com");

    when(jwt.getAudience()).thenReturn(audience);
    when(jwt.getId()).thenReturn(null);
    testError("jti claim not found in token");

    when(jwt.getId()).thenReturn("edf226b4-d84c-4692-815c-973213d47c54");
    assertEquals(success, validator.validate(jwt));
  }
}
