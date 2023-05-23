/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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
