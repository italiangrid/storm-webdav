// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.validator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.italiangrid.storm.webdav.config.OAuthProperties.AuthorizationServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

  public static final Logger LOG = LoggerFactory.getLogger(AudienceValidator.class);

  private final Set<String> requiredAudiences = new HashSet<>();

  private static final OAuth2Error INVALID_AUDIENCE_ERROR =
      new OAuth2Error(
          "invalid_audience",
          "The token audience does not match audience requirements defined for this server",
          null);

  private static final OAuth2TokenValidatorResult SUCCESS = OAuth2TokenValidatorResult.success();

  private static final OAuth2TokenValidatorResult INVALID_AUDIENCE =
      OAuth2TokenValidatorResult.failure(INVALID_AUDIENCE_ERROR);

  public AudienceValidator(AuthorizationServer server) {
    Objects.requireNonNull(server.getAudiences(), "null audiences");
    Assert.notEmpty(server.getAudiences(), "empty audiences");
    requiredAudiences.addAll(server.getAudiences());
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {

    if (jwt.getAudience() == null || jwt.getAudience().isEmpty()) {
      return SUCCESS;
    }

    for (String audience : requiredAudiences) {
      if (jwt.getAudience().contains(audience)) {
        return SUCCESS;
      }
    }

    LOG.debug(
        "Audience check failed. Token audience: {}, local audience: {}",
        jwt.getAudience(),
        requiredAudiences);

    return INVALID_AUDIENCE;
  }
}
