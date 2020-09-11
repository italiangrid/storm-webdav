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
package org.italiangrid.storm.webdav.config;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.italiangrid.storm.webdav.authz.PathAuthzPolicyParser;
import org.italiangrid.storm.webdav.authz.VOMSFQANAuthority;
import org.italiangrid.storm.webdav.authz.VOMSVOAuthority;
import org.italiangrid.storm.webdav.authz.VOMSVOMapAuthority;
import org.italiangrid.storm.webdav.authz.X509SubjectAuthority;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPolicy;
import org.italiangrid.storm.webdav.authz.pdp.principal.AnonymousUser;
import org.italiangrid.storm.webdav.authz.pdp.principal.AnyAuthenticatedUser;
import org.italiangrid.storm.webdav.authz.pdp.principal.Anyone;
import org.italiangrid.storm.webdav.authz.pdp.principal.AuthorityHolder;
import org.italiangrid.storm.webdav.authz.pdp.principal.PrincipalMatcher;
import org.italiangrid.storm.webdav.authz.pdp.principal.PrincipalMatcherDebugWrapper;
import org.italiangrid.storm.webdav.authz.util.CustomHttpMethodMatcher;
import org.italiangrid.storm.webdav.authz.util.ReadonlyHttpMethodMatcher;
import org.italiangrid.storm.webdav.authz.util.WriteHttpMethodMatcher;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.Action;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType;
import org.italiangrid.storm.webdav.oauth.authority.JwtGroupAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtIssuerAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtScopeAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtSubjectAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

@Service
public class FineGrainedAuthzPolicyParser implements PathAuthzPolicyParser {

  final ServiceConfigurationProperties properties;
  final StorageAreaConfiguration saConfig;

  @Autowired
  public FineGrainedAuthzPolicyParser(ServiceConfigurationProperties properties,
      StorageAreaConfiguration saConfig) {
    this.properties = properties;
    this.saConfig = saConfig;
  }

  Supplier<IllegalArgumentException> unknownStorageArea(String saName) {
    return () -> new IllegalArgumentException("Unknown storage area: " + saName);
  }

  StorageAreaInfo getStorageAreaInfo(String saName) {
    return saConfig.getStorageAreaInfo()
      .stream()
      .filter(sa -> sa.name().equals(saName))
      .findAny()
      .orElseThrow(unknownStorageArea(saName));
  }

  String matcherPath(String accessPoint, String path) {
    return String.format("%s/%s", accessPoint, path).replaceAll("\\/\\+", "/");
  }


  Supplier<RequestMatcher> matcherByActionSupplier(Action a, String pattern) {
    return () -> {
      if (Action.ALL.equals(a)) {
        return new AntPathRequestMatcher(pattern);
      } else if (Action.READ.equals(a)) {
        return new ReadonlyHttpMethodMatcher(pattern);
      } else if (Action.WRITE.equals(a)) {
        return new WriteHttpMethodMatcher(pattern);
      } else if (Action.DELETE.equals(a)) {
        return new AntPathRequestMatcher(pattern, "DELETE");
      } else if (Action.LIST.equals(a)) {
        return new AndRequestMatcher(new CustomHttpMethodMatcher(newHashSet("PROPFIND")),
            new AntPathRequestMatcher(pattern));
      } else {
        throw new IllegalArgumentException("Unknown action: " + a);
      }
    };
  }

  void parseAction(Action a, FineGrainedAuthzPolicyProperties policyProperties,
      PathAuthorizationPolicy.Builder builder) {

    List<String> paths = policyProperties.getPaths();

    for (String ap : getStorageAreaInfo(policyProperties.getSa()).accessPoints()) {
      if (policyProperties.getPaths().isEmpty()) {
        builder.withRequestMatcher(matcherByActionSupplier(a, ap + "/**").get());
      } else {
        paths.forEach(
            p -> builder.withRequestMatcher(matcherByActionSupplier(a, matcherPath(ap, p)).get()));
      }
    }
  }

  PrincipalMatcher parsePrincipal(PrincipalProperties p) {
    PrincipalMatcher matcher = null;

    if (PrincipalType.ANONYMOUS.equals(p.getType())) {
      matcher = new AnonymousUser();
    } else if (PrincipalType.ANY_AUTHENTICATED_USER.equals(p.getType())) {
      matcher = new AnyAuthenticatedUser();
    } else if (PrincipalType.ANYONE.equals(p.getType())) {
      matcher = new Anyone();
    } else if (PrincipalType.FQAN.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(new VOMSFQANAuthority(p.getParams().get("fqan")));
    } else if (PrincipalType.JWT_GROUP.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(
          new JwtGroupAuthority(p.getParams().get("iss"), p.getParams().get("group")));
    } else if (PrincipalType.JWT_SCOPE.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(
          new JwtScopeAuthority(p.getParams().get("iss"), p.getParams().get("scope")));
    } else if (PrincipalType.JWT_ISSUER.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(new JwtIssuerAuthority(p.getParams().get("iss")));
    } else if (PrincipalType.JWT_SUBJECT.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(
          new JwtSubjectAuthority(p.getParams().get("iss"), p.getParams().get("sub")));
    } else if (PrincipalType.VO.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(new VOMSVOAuthority(p.getParams().get("vo")));
    } else if (PrincipalType.VO_MAP.equals(p.getType())) {
      matcher = AuthorityHolder.fromAuthority(new VOMSVOMapAuthority(p.getParams().get("vo")));
    } else if (PrincipalType.X509_SUBJECT.equals(p.getType())) {
      matcher =
          AuthorityHolder.fromAuthority(new X509SubjectAuthority(p.getParams().get("subject")));
    }
    return new PrincipalMatcherDebugWrapper(matcher);
  }

  PathAuthorizationPolicy parsePolicy(FineGrainedAuthzPolicyProperties policy) {

    PathAuthorizationPolicy.Builder builder = PathAuthorizationPolicy.builder();

    builder.withId(UUID.randomUUID().toString())
      .withSa(policy.getSa())
      .withEffect(policy.getEffect())
      .withDescription(policy.getDescription());

    policy.getActions().forEach(a -> parseAction(a, policy, builder));

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
