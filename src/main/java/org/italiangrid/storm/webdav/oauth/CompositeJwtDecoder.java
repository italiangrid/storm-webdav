/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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
package org.italiangrid.storm.webdav.oauth;

import static java.lang.String.format;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

public class CompositeJwtDecoder implements JwtDecoder {
  private static final String DECODING_ERROR_MESSAGE_TEMPLATE =
      "An error occurred while attempting to decode the Jwt: %s";

  private static final String UNKNOWN_ISSUER_TEMPLATE = "Unknown token issuer: %s";

  final Map<String, JwtDecoder> decoders;

  public CompositeJwtDecoder(Map<String, JwtDecoder> decoders) {
    this.decoders = decoders;
  }

  protected JwtDecoder resolveDecoder(String token) throws JwtException {

    JWT jwt = parse(token);

    try {
      String issuer = jwt.getJWTClaimsSet().getIssuer();
      Optional<JwtDecoder> decoder = Optional.ofNullable(decoders.get(issuer));
      return decoder.orElseThrow(() -> new JwtException(format(UNKNOWN_ISSUER_TEMPLATE, issuer)));
    } catch (ParseException e) {
      throw new JwtException(format(DECODING_ERROR_MESSAGE_TEMPLATE, e.getMessage()));
    }

  }


  @Override
  public Jwt decode(String token) throws JwtException {
    JwtDecoder decoder = resolveDecoder(token);
    return decoder.decode(token);
  }

  private JWT parse(String token) {
    try {
      return JWTParser.parse(token);
    } catch (Exception ex) {
      throw new JwtException(format(DECODING_ERROR_MESSAGE_TEMPLATE, ex.getMessage()), ex);
    }
  }

}
