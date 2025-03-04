// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.pdp;

import java.util.List;

@FunctionalInterface
public interface PathAuthorizationPolicyRepository {
  List<PathAuthorizationPolicy> getPolicies();
}
