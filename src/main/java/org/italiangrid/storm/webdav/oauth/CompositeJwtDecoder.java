// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth;

import static java.lang.String.format;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

public class CompositeJwtDecoder implements JwtDecoder {

  private static final String DECODING_ERROR_MESSAGE_TEMPLATE =
      "An error occurred while attempting to decode the Jwt: %s";

  public static final Logger LOG = LoggerFactory.getLogger(CompositeJwtDecoder.class);

  final LoadingCache<String, JwtDecoder> decoders;

  public CompositeJwtDecoder(LoadingCache<String, JwtDecoder> decoders) {
    this.decoders = decoders;
  }

  protected JwtDecoder resolveDecoder(String token) {

    String issuer = resolveIssuerFromToken(token);
    try {
      return decoders.get(issuer);
    } catch (ExecutionException | UncheckedExecutionException e) {
      LOG.warn("Error resolving OAuth issuer configuration for {}", issuer);
      if (LOG.isDebugEnabled()) {
        LOG.warn("Error resolving OAuth issuer configuration for {}", issuer, e);
      }
      throw new UnknownTokenIssuerError(issuer);
    }
  }

  @Override
  public Jwt decode(String token) {
    JwtDecoder decoder = resolveDecoder(token);
    return decoder.decode(token);
  }

  private String resolveIssuerFromToken(String token) {
    try {
      JWT jwt;
      jwt = JWTParser.parse(token);
      return jwt.getJWTClaimsSet().getIssuer();
    } catch (ParseException e) {
      if (LOG.isDebugEnabled()) {
        LOG.error(format(DECODING_ERROR_MESSAGE_TEMPLATE, e.getMessage()));
      }
      throw new JwtException(format(DECODING_ERROR_MESSAGE_TEMPLATE, e.getMessage()), e);
    }
  }
}
