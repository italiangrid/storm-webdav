// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import java.util.List;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPolicy;

@FunctionalInterface
public interface PathAuthzPolicyParser {
  List<PathAuthorizationPolicy> parsePolicies();
}
