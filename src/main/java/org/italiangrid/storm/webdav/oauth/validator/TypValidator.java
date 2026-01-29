// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.validator;

import java.util.List;
import org.italiangrid.storm.webdav.config.OAuthProperties.AuthorizationServer;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JoseHeaderNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtValidators;

public class TypValidator implements OAuth2TokenValidator<Jwt> {

  OAuth2TokenValidator<Jwt> jwtValidator;

  OAuth2TokenValidator<Jwt> atJwtValidator;

  public TypValidator(AuthorizationServer as) {
    jwtValidator = JwtValidators.createDefaultWithIssuer(as.issuer());
    if (!as.audiences().isEmpty()) {
      atJwtValidator =
          JwtValidators.createAtJwtValidator()
              .issuer(as.issuer())
              .validators(v -> v.put(JwtClaimNames.AUD, new AudienceValidator(as, true)))
              .validators(v -> v.put("client_id", new RequireClaimValidator("client_id")))
              .build();
    }
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt token) {
    if (atJwtValidator != null) {
      String typ = (String) token.getHeaders().get(JoseHeaderNames.TYP);
      for (String validType : List.of("at+jwt", "application/at+jwt")) {
        if (validType.equalsIgnoreCase(typ)) {
          return atJwtValidator.validate(token);
        }
      }
    }
    return jwtValidator.validate(token);
  }

  // Copied from
  // https://github.com/spring-projects/spring-security/blob/main/oauth2/oauth2-jose/src/main/java/org/springframework/security/oauth2/jwt/JwtValidators.java
  private static final class RequireClaimValidator implements OAuth2TokenValidator<Jwt> {

    private final String claimName;

    RequireClaimValidator(String claimName) {
      this.claimName = claimName;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
      if (token.getClaim(this.claimName) == null) {
        return OAuth2TokenValidatorResult.failure(
            new OAuth2Error(
                OAuth2ErrorCodes.INVALID_TOKEN,
                this.claimName + " must have a value",
                "https://datatracker.ietf.org/doc/html/rfc9068#name-data-structure"));
      }
      return OAuth2TokenValidatorResult.success();
    }
  }
}
