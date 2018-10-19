package org.italiangrid.storm.webdav.oauth.authzserver.web;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonInclude(NON_EMPTY)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AuthzServerMetadata {

  private String issuer;
  private String tokenEndpoint;

  private final List<String> responseTypesSupported;

  private final List<String> grantTypesSupported;

  private final List<String> tokenEndpointAuthMethodsSupported;

  public AuthzServerMetadata() {
    responseTypesSupported = Arrays.asList("token");
    grantTypesSupported = Arrays.asList("client_credentials");
    tokenEndpointAuthMethodsSupported = Arrays.asList("gsi_voms");
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public String getTokenEndpoint() {
    return tokenEndpoint;
  }

  public void setTokenEndpoint(String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
  }

  public List<String> getResponseTypesSupported() {
    return responseTypesSupported;
  }

  public List<String> getGrantTypesSupported() {
    return grantTypesSupported;
  }

  public List<String> getTokenEndpointAuthMethodsSupported() {
    return tokenEndpointAuthMethodsSupported;
  }
}
