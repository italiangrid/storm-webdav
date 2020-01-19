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
package org.italiangrid.storm.webdav.oauth;

import static java.util.Objects.isNull;
import static org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer.CLAIM_AUTHORITIES;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.oauth.authority.OAuthGroupAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@Component
public class StormJwtAuthenticationConverter extends JwtAuthenticationConverter {

  final Multimap<String, GrantedAuthority> authzMap = ArrayListMultimap.create();
  final AuthorizationServerProperties authzServerProperties;

  public static final String[] OAUTH_GROUP_CLAIM_NAMES = {"groups", "wlcg.groups"};
  public static final String SCOPE_CLAIM_NAME = "scope";

  protected void addSaGrantedAuthorities(StorageAreaInfo sa, String issuer) {

    if (sa.orgsGrantReadPermission()) {
      authzMap.put(issuer, SAPermission.canRead(sa.name()));
    }


    if (sa.orgsGrantWritePermission()) {
      authzMap.put(issuer, SAPermission.canWrite(sa.name()));
    }
  }

  @Autowired
  public StormJwtAuthenticationConverter(StorageAreaConfiguration conf,
      ServiceConfigurationProperties props) {
    authzServerProperties = props.getAuthzServer();
    for (StorageAreaInfo sa : conf.getStorageAreaInfo()) {
      if (!isNull(sa.orgs())) {
        sa.orgs().forEach(i -> addSaGrantedAuthorities(sa, i));
      }
    }
  }


  protected Collection<GrantedAuthority> extractAuthoritiesExternalAuthzServer(String issuer) {
    return authzMap.get(issuer);
  }


  protected Set<GrantedAuthority> extractAuthoritiesLocalAuthzServer(Jwt jwt) {
    Set<GrantedAuthority> authorities = Sets.newHashSet();

    jwt.getClaimAsStringList(CLAIM_AUTHORITIES)
      .forEach(a -> authorities.add(SAPermission.fromString(a)));

    return authorities;
  }

  protected boolean isLocalAuthzServer(String issuer) {
    return issuer.equals(authzServerProperties.getIssuer());
  }


  protected Set<GrantedAuthority> extractOauthScopeAuthorities(Jwt jwt) {

    Set<GrantedAuthority> scopeAuthorities = Sets.newHashSet();

    if (!Objects.isNull(jwt.getClaimAsString(SCOPE_CLAIM_NAME))) {
      String tokenIssuer = jwt.getClaimAsString(JwtClaimNames.ISS);

      String[] scopes = jwt.getClaimAsString(SCOPE_CLAIM_NAME).split(" ");
      for (String s : scopes) {
        scopeAuthorities.add(new OAuthGroupAuthority(tokenIssuer, s));
      }
    }
    
    return scopeAuthorities;
  }

  protected Set<GrantedAuthority> extractOauthGroupAuthorities(Jwt jwt) {

    Set<GrantedAuthority> groupAuthorities = Sets.newHashSet();

    String tokenIssuer = jwt.getClaimAsString(JwtClaimNames.ISS);

    for (String groupClaim : OAUTH_GROUP_CLAIM_NAMES) {
      if (jwt.containsClaim(groupClaim)) {
        jwt.getClaimAsStringList(groupClaim)
          .forEach(gc -> groupAuthorities.add(new OAuthGroupAuthority(tokenIssuer, gc)));
        break;
      }
    }
    return groupAuthorities;
  }

  @Override
  protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

    String issuer = jwt.getIssuer().toString();

    if (isLocalAuthzServer(issuer)) {
      return extractAuthoritiesLocalAuthzServer(jwt);
    }

    Set<GrantedAuthority> authorities = Sets.newHashSet();

    authorities.addAll(extractAuthoritiesExternalAuthzServer(issuer));
    authorities.addAll(extractOauthGroupAuthorities(jwt));
    authorities.addAll(extractOauthScopeAuthorities(jwt));

    return authorities;
  }
}
