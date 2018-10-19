package org.italiangrid.storm.webdav.authn;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.WebAttributes;
import org.springframework.util.StringUtils;

public class ErrorPageAuthenticationEntryPoint implements AuthenticationEntryPoint {

  final String errorPage = "/errors/401";

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {

    HttpStatus status = HttpStatus.UNAUTHORIZED;
    Map<String, String> parameters = new LinkedHashMap<>();

    response.setStatus(status.value());

    if (authException instanceof OAuth2AuthenticationException) {
      OAuth2Error error = ((OAuth2AuthenticationException) authException).getError();

      parameters.put("error", error.getErrorCode());

      if (StringUtils.hasText(error.getDescription())) {
        parameters.put("error_description", error.getDescription());
      }

      if (StringUtils.hasText(error.getUri())) {
        parameters.put("error_uri", error.getUri());
      }

      if (error instanceof BearerTokenError) {
        BearerTokenError bearerTokenError = (BearerTokenError) error;

        if (StringUtils.hasText(bearerTokenError.getScope())) {
          parameters.put("scope", bearerTokenError.getScope());
        }

        status = ((BearerTokenError) error).getHttpStatus();
        response.setStatus(status.value());
      }
    } else {

      request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, authException);

      RequestDispatcher dispatcher = request.getRequestDispatcher(errorPage);
      dispatcher.forward(request, response);
    }
  }

}
