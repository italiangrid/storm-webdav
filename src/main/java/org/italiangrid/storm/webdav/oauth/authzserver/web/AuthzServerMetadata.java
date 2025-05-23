// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver.web;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Arrays;
import java.util.List;

@JsonInclude(NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
