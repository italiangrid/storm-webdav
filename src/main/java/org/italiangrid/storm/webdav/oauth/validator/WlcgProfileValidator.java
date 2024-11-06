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
package org.italiangrid.storm.webdav.oauth.validator;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import com.google.common.collect.Sets;

public class WlcgProfileValidator implements OAuth2TokenValidator<Jwt> {

  public static final Logger LOG = LoggerFactory.getLogger(AudienceValidator.class);

  public static final String WLCG_VER_CLAIM = "wlcg.ver";
  public static final String SCOPE_CLAIM = "scope";

  public static final Set<String> WLCG_PROFILE_SUPPORTED_VERSIONS =
      Collections.unmodifiableSet(Sets.newHashSet("1.0"));

  private static final OAuth2Error INVALID_PROFILE_VERSION =
      new OAuth2Error("invalid_token", "Unsupported WLCG token profile version", null);

  private static final OAuth2Error MISSING_SCOPE =
      new OAuth2Error("invalid_token", "scope claim not found in token", null);

  private static final OAuth2Error MISSING_NBF =
      new OAuth2Error("invalid_token", "nbf claim not found in token", null);

  private static final OAuth2Error MISSING_EXP =
      new OAuth2Error("invalid_token", "exp claim not found in token", null);

  private static final OAuth2Error MISSING_SUB =
      new OAuth2Error("invalid_token", "sub claim not found in token", null);

  private static final OAuth2Error MISSING_AUD =
      new OAuth2Error("invalid_token", "aud claim not found in token", null);

  private static final OAuth2Error MISSING_JTI =
      new OAuth2Error("invalid_token", "jti claim not found in token", null);

  private static final OAuth2TokenValidatorResult SUCCESS = OAuth2TokenValidatorResult.success();

  @Override
  public OAuth2TokenValidatorResult validate(Jwt token) {

    if (isNullOrEmpty(token.getClaimAsString(WLCG_VER_CLAIM))) {
      // This validator does not apply to non-WLCG tokens
      return SUCCESS;
    }

    String wlcgVersion = token.getClaimAsString(WLCG_VER_CLAIM);

    if (!WLCG_PROFILE_SUPPORTED_VERSIONS.contains(wlcgVersion)) {
      return OAuth2TokenValidatorResult.failure(INVALID_PROFILE_VERSION);
    }

    if (Boolean.FALSE.equals(token.containsClaim(SCOPE_CLAIM))) {
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
