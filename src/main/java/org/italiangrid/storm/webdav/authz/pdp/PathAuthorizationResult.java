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

import java.util.Optional;

public class PathAuthorizationResult {

  public enum Decision {
    DENY,
    PERMIT,
    NOT_APPLICABLE,
    INDETERMINATE
  }

  final Decision decision;
  final PathAuthorizationPolicy policy;
  final String message;

  private PathAuthorizationResult(Decision d, PathAuthorizationPolicy p, String message) {
    this.decision = d;
    this.policy = p;
    this.message = message;
  }

  private PathAuthorizationResult(Decision d, PathAuthorizationPolicy p) {
    this(d, p, null);
  }

  public Decision getDecision() {
    return decision;
  }

  public Optional<PathAuthorizationPolicy> getPolicy() {
    return Optional.ofNullable(policy);
  }

  public Optional<String> getMessage() {
    return Optional.ofNullable(message);
  }

  public static PathAuthorizationResult deny(String message) {
    return new PathAuthorizationResult(Decision.DENY, null, message);
  }

  public static PathAuthorizationResult deny() {
    return new PathAuthorizationResult(Decision.DENY, null);
  }

  public static PathAuthorizationResult permit() {
    return new PathAuthorizationResult(Decision.PERMIT, null);
  }

  public static PathAuthorizationResult deny(PathAuthorizationPolicy p) {
    return new PathAuthorizationResult(Decision.DENY, p);
  }

  public static PathAuthorizationResult permit(PathAuthorizationPolicy p) {
    return new PathAuthorizationResult(Decision.PERMIT, p);
  }

  public static PathAuthorizationResult notApplicable() {
    return new PathAuthorizationResult(Decision.NOT_APPLICABLE, null);
  }

  public static PathAuthorizationResult indeterminate() {
    return new PathAuthorizationResult(Decision.INDETERMINATE, null);
  }
  
  public static PathAuthorizationResult indeterminate(String message) {
    return new PathAuthorizationResult(Decision.INDETERMINATE, null, message);
  }

  public static PathAuthorizationResult fromPolicy(PathAuthorizationPolicy p) {
    if (p.getEffect() == PolicyEffect.PERMIT) {
      return permit(p);
    } else {
      return deny(p);
    }
  }
}
