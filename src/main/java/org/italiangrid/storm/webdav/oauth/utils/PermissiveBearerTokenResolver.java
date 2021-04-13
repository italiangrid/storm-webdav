/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.oauth.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.util.StringUtils;

/**
 * 
 * Does not raise error when a bearer token is found both as parameter and in the Authorization
 * header. Request parameter takes precedence
 *
 */
public class PermissiveBearerTokenResolver implements BearerTokenResolver {

  private static final Pattern authorizationPattern =
      Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+)=*$", Pattern.CASE_INSENSITIVE);


  private static String resolveFromRequestParameters(HttpServletRequest request) {
    String[] values = request.getParameterValues("access_token");
    if (values == null || values.length == 0) {
      return null;
    }

    if (values.length == 1) {
      return values[0];
    }

    BearerTokenError error = new BearerTokenError(BearerTokenErrorCodes.INVALID_REQUEST,
        HttpStatus.BAD_REQUEST, "Found multiple bearer tokens in the request",
        "https://tools.ietf.org/html/rfc6750#section-3.1");
    throw new OAuth2AuthenticationException(error);
  }

  private static String resolveFromAuthorizationHeader(HttpServletRequest request) {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
      Matcher matcher = authorizationPattern.matcher(authorization);

      if (!matcher.matches()) {
        BearerTokenError error =
            new BearerTokenError(BearerTokenErrorCodes.INVALID_TOKEN, HttpStatus.UNAUTHORIZED,
                "Bearer token is malformed", "https://tools.ietf.org/html/rfc6750#section-3.1");
        throw new OAuth2AuthenticationException(error);
      }

      return matcher.group("token");
    }
    return null;
  }

  @Override
  public String resolve(HttpServletRequest request) {
    String authorizationHeaderToken = resolveFromAuthorizationHeader(request);
    String parameterToken = resolveFromRequestParameters(request);

    if (parameterToken != null) {
      return parameterToken;
    }

    return authorizationHeaderToken;
  }

}
