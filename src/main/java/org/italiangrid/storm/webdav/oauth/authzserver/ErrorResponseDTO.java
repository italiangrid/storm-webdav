// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ErrorResponseDTO(String error, String errorDescription) {

  public static final String UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
  public static final String INVALID_REQUEST = "invalid_request";
  public static final String INVALID_SCOPE = "invalid_scope";
}
