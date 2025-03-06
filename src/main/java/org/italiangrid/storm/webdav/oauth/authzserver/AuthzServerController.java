// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver;

import static java.lang.String.format;
import static org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest.GRANT_TYPE_NOT_FOUND;
import static org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest.INVALID_GRANT_TYPE;
import static org.italiangrid.storm.webdav.oauth.authzserver.ErrorResponseDTO.INVALID_REQUEST;
import static org.italiangrid.storm.webdav.oauth.authzserver.ErrorResponseDTO.INVALID_SCOPE;
import static org.italiangrid.storm.webdav.oauth.authzserver.ErrorResponseDTO.UNSUPPORTED_GRANT_TYPE;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.italiangrid.storm.webdav.oauth.authzserver.error.InvalidScopeError;
import org.italiangrid.storm.webdav.oauth.authzserver.error.InvalidTokenRequestError;
import org.italiangrid.storm.webdav.oauth.authzserver.error.UnsupportedGrantTypeError;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
@ConditionalOnExpression("${storm.authz-server.enabled}")
@RequestMapping("/oauth")
public class AuthzServerController {

  final TokenIssuerService tokenService;

  public AuthzServerController(TokenIssuerService tis) {
    this.tokenService = tis;
  }

  protected void handleValidationError(FieldError e) {

    if (GRANT_TYPE_NOT_FOUND.equals(e.getDefaultMessage())) {
      throw new InvalidTokenRequestError(e.getDefaultMessage());
    }
    if (INVALID_GRANT_TYPE.equals(e.getDefaultMessage())) {
      throw new UnsupportedGrantTypeError(
          format("%s: %s", e.getDefaultMessage(), e.getRejectedValue()));
    }
    throw new InvalidScopeError(e.getDefaultMessage() != null ? e.getDefaultMessage() : "");
  }

  @PreAuthorize("#storm.isVOMSAuthenticated()")
  @PostMapping(
      value = "/token",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public TokenResponseDTO getAccessToken(
      @Valid AccessTokenRequest tokenRequest,
      BindingResult bindingResult,
      Authentication authentication) {

    if (bindingResult.hasErrors()) {
      handleValidationError(bindingResult.getFieldError());
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
