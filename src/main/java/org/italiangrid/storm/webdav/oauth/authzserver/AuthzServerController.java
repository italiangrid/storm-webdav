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
package org.italiangrid.storm.webdav.oauth.authzserver;

import static java.lang.String.format;
import static org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest.GRANT_TYPE_NOT_FOUND;
import static org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest.INVALID_GRANT_TYPE;
import static org.italiangrid.storm.webdav.oauth.authzserver.ErrorResponseDTO.INVALID_REQUEST;
import static org.italiangrid.storm.webdav.oauth.authzserver.ErrorResponseDTO.INVALID_SCOPE;
import static org.italiangrid.storm.webdav.oauth.authzserver.ErrorResponseDTO.UNSUPPORTED_GRANT_TYPE;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.italiangrid.storm.webdav.oauth.authzserver.error.InvalidScopeError;
import org.italiangrid.storm.webdav.oauth.authzserver.error.InvalidTokenRequestError;
import org.italiangrid.storm.webdav.oauth.authzserver.error.UnsupportedGrantTypeError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
public class AuthzServerController {

  final TokenIssuerService tokenService;

  public AuthzServerController(TokenIssuerService tis) {
    this.tokenService = tis;
  }

  protected void handleValidationError(BindingResult bindingResult) {

    FieldError e = bindingResult.getFieldError();
    
    if (e.getDefaultMessage().equals(GRANT_TYPE_NOT_FOUND)) {
      throw new InvalidTokenRequestError(e.getDefaultMessage());
    } else if (e.getDefaultMessage().equals(INVALID_GRANT_TYPE)) {
      throw new UnsupportedGrantTypeError(format("%s: %s", e.getDefaultMessage(), e.getRejectedValue()));
    } else {
      throw new InvalidScopeError(e.getDefaultMessage());
    }
  }


  @PreAuthorize("#storm.isVOMSAuthenticated()")
  @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public TokenResponseDTO getAccessToken(@Valid AccessTokenRequest tokenRequest,
      BindingResult bindingResult, Authentication authentication) {

    if (bindingResult.hasErrors()) {
      handleValidationError(bindingResult);
    }

    return tokenService.createAccessToken(tokenRequest, authentication);
  }


  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UnsupportedGrantTypeError.class)
  public ErrorResponseDTO unsupportedGrantType(HttpServletRequest req, Exception ex) {
    return ErrorResponseDTO.from(UNSUPPORTED_GRANT_TYPE, ex.getMessage());
  }

  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InvalidTokenRequestError.class)
  public ErrorResponseDTO invalidTokenRequestError(HttpServletRequest req, Exception ex) {
    return ErrorResponseDTO.from(INVALID_REQUEST, ex.getMessage());
  }

  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InvalidScopeError.class)
  public ErrorResponseDTO invalidScopeError(HttpServletRequest req, Exception ex) {
    return ErrorResponseDTO.from(INVALID_SCOPE, ex.getMessage());
  }
}
