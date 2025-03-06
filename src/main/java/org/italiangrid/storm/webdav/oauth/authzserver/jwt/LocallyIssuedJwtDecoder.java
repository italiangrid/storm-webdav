// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver.jwt;

import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;

public class LocallyIssuedJwtDecoder implements JwtDecoder {

  private static final String DECODING_ERROR_MESSAGE_TEMPLATE =
      "An error occurred while attempting to decode the Jwt: %s";

  private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

  private final Converter<Map<String, Object>, Map<String, Object>> claimSetConverter =
      MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());

  private final OAuth2TokenValidator<Jwt> jwtValidator;

  public LocallyIssuedJwtDecoder(AuthorizationServerProperties props) {
    ImmutableSecret<SecurityContext> secret = new ImmutableSecret<>(props.getSecret().getBytes());
    JWSKeySelector<SecurityContext> jwsKeySelector =
        new JWSVerificationKeySelector<>(DefaultJwtTokenIssuer.JWS_ALGO, secret);

    jwtProcessor = new DefaultJWTProcessor<>();
    jwtProcessor.setJWSKeySelector(jwsKeySelector);
    jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {});
    jwtValidator = JwtValidators.createDefaultWithIssuer(props.getIssuer());
  }

  @Override
  public Jwt decode(String token) {
    JWT jwt = parse(token);
    if (jwt instanceof SignedJWT) {
      Jwt createdJwt = this.createJwt(token, jwt);
      return this.validateJwt(createdJwt);
    }
    throw new JwtException("Unsupported algorithm of " + jwt.getHeader().getAlgorithm());
  }

  private Jwt validateJwt(Jwt jwt) {
    OAuth2TokenValidatorResult result = this.jwtValidator.validate(jwt);
    if (result.hasErrors()) {
      String description = result.getErrors().iterator().next().getDescription();
      throw new JwtValidationException(
          String.format(DECODING_ERROR_MESSAGE_TEMPLATE, description), result.getErrors());
    }

    return jwt;
  }

  private Jwt createJwt(String token, JWT parsedJwt) {
    Jwt jwt;

    try {
      // Verify the signature
      JWTClaimsSet jwtClaimsSet = this.jwtProcessor.process(parsedJwt, null);

      Map<String, Object> headers = new LinkedHashMap<>(parsedJwt.getHeader().toJSONObject());
      Map<String, Object> claims = this.claimSetConverter.convert(jwtClaimsSet.getClaims());
      if (claims == null) {
        throw new Exception("Error on claims set conversion");
      }
      Instant expiresAt = (Instant) claims.get(JwtClaimNames.EXP);
      Instant issuedAt = (Instant) claims.get(JwtClaimNames.IAT);
      jwt = new Jwt(token, issuedAt, expiresAt, headers, claims);
    } catch (RemoteKeySourceException ex) {
      if (ex.getCause() instanceof ParseException) {
        throw new JwtException(String.format(DECODING_ERROR_MESSAGE_TEMPLATE, "Malformed Jwk set"));
      } else {
        throw new JwtException(String.format(DECODING_ERROR_MESSAGE_TEMPLATE, ex.getMessage()), ex);
      }
    } catch (Exception ex) {
      if (ex.getCause() instanceof ParseException) {
        throw new JwtException(String.format(DECODING_ERROR_MESSAGE_TEMPLATE, "Malformed payload"));
      } else {
        throw new JwtException(String.format(DECODING_ERROR_MESSAGE_TEMPLATE, ex.getMessage()), ex);
      }
    }

    return jwt;
  }

  private JWT parse(String token) {
    try {
      return JWTParser.parse(token);
    } catch (Exception ex) {
      throw new JwtException(String.format(DECODING_ERROR_MESSAGE_TEMPLATE, ex.getMessage()), ex);
    }
  }
}
