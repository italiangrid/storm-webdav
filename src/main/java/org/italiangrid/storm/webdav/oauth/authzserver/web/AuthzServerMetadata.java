/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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
