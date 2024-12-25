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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.pdp.principal.PrincipalMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class PathAuthorizationPolicy {

  public static final Logger LOG = LoggerFactory.getLogger(PathAuthorizationPolicy.class);

  private final String id;
  private final String sa;
  private final String decription;
  private final PolicyEffect effect;
  private final List<RequestMatcher> requestMatchers;
  private final List<PrincipalMatcher> principalMatchers;

  private PathAuthorizationPolicy(Builder builder) {
    this.id = builder.id;
    this.sa = builder.sa;
    this.decription = builder.description;
    this.effect = builder.effect;
    this.requestMatchers = builder.requestMatchers;
    this.principalMatchers = builder.principalMatchers;
  }

  public boolean appliesToRequest(HttpServletRequest request, Authentication authentication) {

    boolean requestMatched = requestMatchers.stream().anyMatch(p -> p.matches(request));

    LOG.debug("Policy {} ({}) matches request: {}", getId(), getDecription(), requestMatched);

    if (!requestMatched) {
      return false;
    }

    boolean principalMatched =
        principalMatchers.stream().anyMatch(m -> m.matchesPrincipal(authentication));

    LOG.debug("Policy {} ({}) matches principal: {}", getId(), getDecription(), principalMatched);


    return principalMatched;

  }

  public String getSa() {
    return sa;
  }

  public PolicyEffect getEffect() {
    return effect;
  }

  public String getId() {
    return id;
  }

  public String getDecription() {
    return decription;
  }


  public List<PrincipalMatcher> getPrincipalMatchers() {
    return principalMatchers;
  }

  public List<RequestMatcher> getRequestMatchers() {
    return requestMatchers;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String id;

    private String sa;

    private String description;

    private PolicyEffect effect = PolicyEffect.DENY;

    private List<RequestMatcher> requestMatchers = new ArrayList<>();
    private List<PrincipalMatcher> principalMatchers = new ArrayList<>();

    public Builder withSa(String sa) {
      this.sa = sa;
      return this;
    }

    public Builder withEffect(PolicyEffect e) {
      this.effect = e;
      return this;
    }

    public Builder withPermit() {
      this.effect = PolicyEffect.PERMIT;
      return this;
    }

    public Builder withDeny() {
      this.effect = PolicyEffect.DENY;
      return this;
    }

    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder withId(String id) {
      this.id = id;
      return this;
    }

    public Builder withRequestMatchers(RequestMatcher... matchers) {
      for (RequestMatcher m : matchers) {
        this.requestMatchers.add(m);
      }
      return this;
    }

    public Builder withRequestMatcher(RequestMatcher m) {
      this.requestMatchers.add(m);
      return this;
    }

    public Builder withPrincipalMatcher(PrincipalMatcher m) {
      this.principalMatchers.add(m);
      return this;
    }

    public PathAuthorizationPolicy build() {
      if (id == null) {
        id = UUID.randomUUID().toString();
      }
      return new PathAuthorizationPolicy(this);
    }
  }

  @Override
  public String toString() {
    return "PathAuthorizationPolicy [id=" + id + ", sa=" + sa + ", decription=" + decription
        + ", effect=" + effect + ", requestMatchers=" + requestMatchers + ", principalMatchers="
        + principalMatchers + "]";
  }

}
