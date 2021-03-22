/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.authz.expression;

import org.italiangrid.storm.webdav.authz.VOMSVOAuthority;
import org.italiangrid.storm.webdav.authz.VOMSVOMapAuthority;
import org.springframework.security.core.Authentication;

public class StormSecurityExpressionMethods {

  final Authentication authentication;

  public StormSecurityExpressionMethods(Authentication authn) {
    this.authentication = authn;
  }

  public boolean isVOMSAuthenticated() {
    return authentication.getAuthorities().stream().anyMatch(VOMSVOAuthority.class::isInstance);
  }

  public boolean hasVoMapAuthorities() {
    return authentication.getAuthorities().stream().anyMatch(VOMSVOMapAuthority.class::isInstance);
  }
}
