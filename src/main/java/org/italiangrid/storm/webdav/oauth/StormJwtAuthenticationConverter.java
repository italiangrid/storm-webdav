/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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
import java.util.Set;

import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@Component
public class StormJwtAuthenticationConverter extends JwtAuthenticationConverter {

  final Multimap<String, GrantedAuthority> authzMap = ArrayListMultimap.create();
  final AuthorizationServerProperties authzServerProperties;

  protected void addSaGrantedAuthorities(String saName, String issuer,
      Boolean orgGrantsWriteAccess) {

    authzMap.put(issuer, SAPermission.canRead(saName));

    if (orgGrantsWriteAccess) {
      authzMap.put(issuer, SAPermission.canWrite(saName));
    }

  }

  @Autowired
  public StormJwtAuthenticationConverter(StorageAreaConfiguration conf,
      ServiceConfigurationProperties props) {

    authzServerProperties = props.getAuthzServer();
    for (StorageAreaInfo sa : conf.getStorageAreaInfo()) {
      if (!isNull(sa.orgs())) {
        sa.orgs()
          .forEach(i -> addSaGrantedAuthorities(sa.name(), i, sa.orgsGrantWritePermission()));
      }
    }
  }


  protected Collection<GrantedAuthority> extractAuthoritiesExternalAuthzServer(String issuer) {
    return authzMap.get(issuer);
  }

  protected Collection<GrantedAuthority> extractAuthoritiesLocalAuthzServer(Jwt jwt) {
    Set<GrantedAuthority> authorities = Sets.newHashSet();

    jwt.getClaimAsStringList(CLAIM_AUTHORITIES)
      .forEach(a -> authorities.add(SAPermission.fromString(a)));

    return authorities;
  }

  protected boolean isLocalAuthzServer(String issuer) {
    return issuer.equals(authzServerProperties.getIssuer());
  }
  @Override
  protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

    String issuer = jwt.getIssuer().toString();

    if (isLocalAuthzServer(issuer)) {
      return extractAuthoritiesLocalAuthzServer(jwt);
    }

    return extractAuthoritiesExternalAuthzServer(issuer);
  }
}
