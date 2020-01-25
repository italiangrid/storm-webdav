package org.italiangrid.storm.webdav.oidc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR,
    reason = "OpenID provider initialization error")
public class OidcProviderError extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public OidcProviderError(String message, Throwable cause) {
    super(message, cause);
  }

}
