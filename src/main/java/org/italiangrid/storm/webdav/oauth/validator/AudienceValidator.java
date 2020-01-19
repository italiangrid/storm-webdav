package org.italiangrid.storm.webdav.oauth.validator;

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

  private final OAuth2Error INVALID_AUDIENCE_ERROR = new OAuth2Error("invalid_audience",
      "The token audience does not match audience requirements defined for this server", null);

  private final OAuth2TokenValidatorResult SUCCESS = OAuth2TokenValidatorResult.success();

  private final OAuth2TokenValidatorResult INVALID_AUDIENCE =
      OAuth2TokenValidatorResult.failure(INVALID_AUDIENCE_ERROR);

  public AudienceValidator(AuthorizationServer server) {
    requiredAudiences.addAll(server.getAudiences());
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {

    for (String audience : requiredAudiences) {
      if (jwt.getAudience().contains(audience)) {
        return SUCCESS;
      }
    }

    LOG.debug("Invalid audience on token: {}", jwt);
    return INVALID_AUDIENCE;
  }

}
