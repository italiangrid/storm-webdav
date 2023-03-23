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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;

import java.util.Set;

import org.italiangrid.storm.webdav.config.OAuthProperties.AuthorizationServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import com.google.common.collect.Sets;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

  public static final Logger LOG = LoggerFactory.getLogger(AudienceValidator.class);

  private final Set<String> requiredAudiences = Sets.newHashSet();

  private static final OAuth2Error INVALID_AUDIENCE_ERROR = new OAuth2Error("invalid_audience",
      "The token audience does not match audience requirements defined for this server", null);

  private static final OAuth2TokenValidatorResult SUCCESS = OAuth2TokenValidatorResult.success();

  private static final OAuth2TokenValidatorResult INVALID_AUDIENCE =
      OAuth2TokenValidatorResult.failure(INVALID_AUDIENCE_ERROR);

  public AudienceValidator(AuthorizationServer server) {
    checkArgument(!isNull(server.getAudiences()), "null audiences");
    checkArgument(!server.getAudiences().isEmpty(), "empty audiences");
    requiredAudiences.addAll(server.getAudiences());
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {

    if (isNull(jwt.getAudience()) || jwt.getAudience().isEmpty()) {
      return SUCCESS;
    }

    for (String audience : requiredAudiences) {
      if (jwt.getAudience().contains(audience)) {
        return SUCCESS;
      }
    }

    LOG.debug("Audience check failed. Token audience: {}, local audience: {}", jwt.getAudience(),
        requiredAudiences);
    
    return INVALID_AUDIENCE;
  }

}
