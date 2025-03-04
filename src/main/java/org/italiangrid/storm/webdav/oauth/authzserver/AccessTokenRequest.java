// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AccessTokenRequest {

  public static final int MAX_PATH_LENGTH = 4096;
  public static final int MAX_SCOPE_LENGTH = 4 * 4096;

  public static final String CLIENT_CREDENTIALS = "client_credentials";
  public static final String GRANT_TYPE_NOT_FOUND = "grant_type not found in request";
  public static final String INVALID_GRANT_TYPE = "Invalid grant type";

  public static final String SCOPE_TOO_LONG =
      "scope exceeds maximum length in characters: " + MAX_SCOPE_LENGTH;

  @NotNull(message = GRANT_TYPE_NOT_FOUND)
  @Pattern(regexp = CLIENT_CREDENTIALS, message = INVALID_GRANT_TYPE)
  private String grant_type;

  @Size(max = MAX_SCOPE_LENGTH, message = SCOPE_TOO_LONG)
  private String scope;

  private Long lifetime;

  public String getGrant_type() {
    return grant_type;
  }

  public void setGrant_type(String grant_type) {
    this.grant_type = grant_type;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public Long getLifetime() {
    return lifetime;
  }

  public void setLifetime(Long lifetime) {
    this.lifetime = lifetime;
  }
}
