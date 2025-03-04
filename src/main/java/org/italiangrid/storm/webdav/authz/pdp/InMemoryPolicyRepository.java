// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.pdp;

import java.util.List;

public class InMemoryPolicyRepository implements PathAuthorizationPolicyRepository {

  private final List<PathAuthorizationPolicy> policies;
  public InMemoryPolicyRepository(List<PathAuthorizationPolicy> policies) {
    this.policies = policies;
  }

  @Override
  public List<PathAuthorizationPolicy> getPolicies() {
    return policies;
  }

}
