// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonInclude(NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ErrorResponseDTO {

  public static final String UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
  public static final String INVALID_REQUEST = "invalid_request";
  public static final String INVALID_SCOPE = "invalid_scope";

  String error;
  String errorDescription;

  public ErrorResponseDTO(String error, String description) {
    this.error = error;
    this.errorDescription = description;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getErrorDescription() {
    return errorDescription;
  }

  public void setErrorDescription(String errorDescription) {
    this.errorDescription = errorDescription;
  }

  public static ErrorResponseDTO from(String error, String errorDescription) {
    return new ErrorResponseDTO(error, errorDescription);
  }
}
