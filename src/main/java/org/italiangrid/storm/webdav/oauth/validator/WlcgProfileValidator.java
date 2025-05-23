// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.validator;

import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

public class WlcgProfileValidator implements OAuth2TokenValidator<Jwt> {

  public static final Logger LOG = LoggerFactory.getLogger(WlcgProfileValidator.class);

  public static final String INVALID_TOKEN_ERROR_CODE = "invalid_token";
  public static final String WLCG_VER_CLAIM = "wlcg.ver";
  public static final String SCOPE_CLAIM = "scope";

  public static final Set<String> WLCG_PROFILE_SUPPORTED_VERSIONS =
      Collections.unmodifiableSet(Set.of("1.0"));

  private static final OAuth2Error INVALID_PROFILE_VERSION =
      new OAuth2Error(INVALID_TOKEN_ERROR_CODE, "Unsupported WLCG token profile version", null);

  private static final OAuth2Error MISSING_SCOPE =
      new OAuth2Error(INVALID_TOKEN_ERROR_CODE, "scope claim not found in token", null);

  private static final OAuth2Error MISSING_NBF =
      new OAuth2Error(INVALID_TOKEN_ERROR_CODE, "nbf claim not found in token", null);

  private static final OAuth2Error MISSING_EXP =
      new OAuth2Error(INVALID_TOKEN_ERROR_CODE, "exp claim not found in token", null);

  private static final OAuth2Error MISSING_SUB =
      new OAuth2Error(INVALID_TOKEN_ERROR_CODE, "sub claim not found in token", null);

  private static final OAuth2Error MISSING_AUD =
      new OAuth2Error(INVALID_TOKEN_ERROR_CODE, "aud claim not found in token", null);

  private static final OAuth2Error MISSING_JTI =
      new OAuth2Error(INVALID_TOKEN_ERROR_CODE, "jti claim not found in token", null);

  private static final OAuth2TokenValidatorResult SUCCESS = OAuth2TokenValidatorResult.success();

  @Override
  public OAuth2TokenValidatorResult validate(Jwt token) {

    if (!StringUtils.hasText(token.getClaimAsString(WLCG_VER_CLAIM))) {
      // This validator does not apply to non-WLCG tokens
      return SUCCESS;
    }

    String wlcgVersion = token.getClaimAsString(WLCG_VER_CLAIM);

    if (!WLCG_PROFILE_SUPPORTED_VERSIONS.contains(wlcgVersion)) {
      return OAuth2TokenValidatorResult.failure(INVALID_PROFILE_VERSION);
    }

    if (!token.hasClaim(SCOPE_CLAIM)) {
      return OAuth2TokenValidatorResult.failure(MISSING_SCOPE);
    }

    if (token.getNotBefore() == null) {
      return OAuth2TokenValidatorResult.failure(MISSING_NBF);
    }

    if (token.getExpiresAt() == null) {
      return OAuth2TokenValidatorResult.failure(MISSING_EXP);
    }

    if (token.getSubject() == null) {
      return OAuth2TokenValidatorResult.failure(MISSING_SUB);
    }

    if (token.getAudience() == null || token.getAudience().isEmpty()) {
      return OAuth2TokenValidatorResult.failure(MISSING_AUD);
    }

    if (token.getId() == null) {
      return OAuth2TokenValidatorResult.failure(MISSING_JTI);
    }

    return SUCCESS;
  }
}
