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
package org.italiangrid.storm.webdav.oauth;

import static java.lang.String.format;

import org.springframework.security.oauth2.jwt.JwtException;

public class UnknownTokenIssuerError extends JwtException {

  
  private static final String UNKNOWN_ISSUER_TEMPLATE = "Unknown token issuer: %s";
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public UnknownTokenIssuerError(String issuer) {
    super(format(UNKNOWN_ISSUER_TEMPLATE,issuer));
  }

}
