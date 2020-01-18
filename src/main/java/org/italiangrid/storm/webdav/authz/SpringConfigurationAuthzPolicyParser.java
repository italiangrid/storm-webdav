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
package org.italiangrid.storm.webdav.authz;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPolicy;
import org.italiangrid.storm.webdav.authz.pdp.principal.AnonymousUser;
import org.italiangrid.storm.webdav.authz.pdp.principal.AnyAuthenticatedUser;
import org.italiangrid.storm.webdav.authz.pdp.principal.Anyone;
import org.italiangrid.storm.webdav.authz.pdp.principal.AuthorityHolder;
import org.italiangrid.storm.webdav.authz.pdp.principal.PrincipalMatcher;
import org.italiangrid.storm.webdav.authz.util.CustomHttpMethodMatcher;
import org.italiangrid.storm.webdav.authz.util.ReadonlyHttpMethodMatcher;
import org.italiangrid.storm.webdav.authz.util.WriteHttpMethodMatcher;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicy;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicy.Action;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicy.Principal;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicy.Principal.PrincipalType;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.oauth.authority.OAuthGroupAuthority;
import org.italiangrid.storm.webdav.oauth.authority.OAuthScopeAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "storm.authz.enable-fine-grained-authz", havingValue = "true")
public class SpringConfigurationAuthzPolicyParser implements PathAuthzPolicyParser {

  final ServiceConfigurationProperties properties;

  @Autowired
  public SpringConfigurationAuthzPolicyParser(ServiceConfigurationProperties properties) {
    this.properties = properties;
  }


  void parseAction(Action a, List<String> paths, PathAuthorizationPolicy.Builder builder) {
    if (Action.ALL.equals(a)) {
      paths.forEach(p -> builder.withRequestMatcher(new AntPathRequestMatcher(p)));
    } else if (Action.READ.equals(a)) {
      paths.forEach(p -> builder.withRequestMatcher(new ReadonlyHttpMethodMatcher(p)));
    } else if (Action.WRITE.equals(a)) {
      paths.forEach(p -> builder.withRequestMatcher(new WriteHttpMethodMatcher(p)));
    } else if (Action.DELETE.equals(a)) {
      paths.forEach(p -> builder.withRequestMatcher(new AntPathRequestMatcher(p, "DELETE")));
    } else if (Action.LIST.equals(a)) {
      paths.forEach(p -> builder.withRequestMatcher(new AndRequestMatcher(
          new CustomHttpMethodMatcher(newHashSet("PROPFIND")), new AntPathRequestMatcher(p))));
    }
  }

  PrincipalMatcher parsePrincipal(Principal p) {
    PrincipalMatcher matcher = null;

    if (PrincipalType.ANONYMOUS.equals(p.getType())) {
      matcher = new AnonymousUser();
    } else if (PrincipalType.ANY_AUTHENTICATED_USER.equals(p.getType())) {
      matcher = new AnyAuthenticatedUser();
    } else if (PrincipalType.ANYONE.equals(p.getType())) {
      matcher = new Anyone();
    } else if (PrincipalType.FQAN.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(new VOMSFQANAuthority(p.getParams().get("fqan")));
    } else if (PrincipalType.OAUTH_GROUP.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(
          new OAuthGroupAuthority(p.getParams().get("iss"), p.getParams().get("group")));
    } else if (PrincipalType.OAUTH_SCOPE.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(
          new OAuthScopeAuthority(p.getParams().get("iss"), p.getParams().get("scope")));
    } else if (PrincipalType.VO.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(new VOMSVOAuthority(p.getParams().get("vo")));
    } else if (PrincipalType.VO_MAP.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(new VOMSVOMapAuthority(p.getParams().get("vo")));
    }
    return matcher;
  }

  PathAuthorizationPolicy parsePolicy(FineGrainedAuthzPolicy policy) {

    PathAuthorizationPolicy.Builder builder = PathAuthorizationPolicy.builder();

    builder.withEffect(policy.getEffect())
      .withId(policy.getId())
      .withDescription(policy.getDescription());

    policy.getActions().forEach(a -> parseAction(a, policy.getPaths(), builder));

    policy.getPrincipals()
      .stream()
      .map(this::parsePrincipal)
      .forEach(builder::withPrincipalMatcher);;

    return builder.build();
  }

  @Override
  public List<PathAuthorizationPolicy> parsePolicies() {

    return properties.getAuthz().getPolicies().stream().map(this::parsePolicy).collect(toList());

  }

}
