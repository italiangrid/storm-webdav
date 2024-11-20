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
package org.italiangrid.storm.webdav.authz.pdp;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "storm.authz.enable-fine-grained-authz", havingValue = "true")
public class DefaultPathAuthorizationPdp implements PathAuthorizationPdp {

  private static final PathAuthorizationResult NOT_APPLICABLE =
      PathAuthorizationResult.notApplicable();

  private final PathAuthorizationPolicyRepository repo;

  @Autowired
  public DefaultPathAuthorizationPdp(PathAuthorizationPolicyRepository repo) {
    this.repo = repo;
  }

  @Override
  public PathAuthorizationResult authorizeRequest(PathAuthorizationRequest authzRequest) {

    final HttpServletRequest request = authzRequest.getRequest();
    final Authentication authentication = authzRequest.getAuthentication();

    return repo.getPolicies()
      .stream()
      .filter(p -> p.appliesToRequest(request, authentication))
      .findFirst()
      .map(PathAuthorizationResult::fromPolicy)
      .orElse(NOT_APPLICABLE);
  }

}
