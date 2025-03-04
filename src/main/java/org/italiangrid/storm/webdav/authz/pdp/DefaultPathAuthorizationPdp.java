// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
