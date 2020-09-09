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

import java.util.Collection;
import java.util.Set;

import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class GrantedAuthoritiesMapperSupport {

  protected final Multimap<String, GrantedAuthority> authzMap = ArrayListMultimap.create();
  protected final AuthorizationServerProperties authzServerProperties;

  public static final String[] OAUTH_GROUP_CLAIM_NAMES = {"groups", "wlcg.groups"};
  public static final String SCOPE_CLAIM_NAME = "scope";

  protected final Set<GrantedAuthority> anonymousGrantedAuthorities = Sets.newHashSet();


  public GrantedAuthoritiesMapperSupport(StorageAreaConfiguration conf,
      ServiceConfigurationProperties props) {

    authzServerProperties = props.getAuthzServer();

    for (StorageAreaInfo sa : conf.getStorageAreaInfo()) {
      if (!isNull(sa.orgs())) {
        sa.orgs().forEach(i -> addSaGrantedAuthorities(sa, i));
      }
    }

    for (StorageAreaInfo sa : conf.getStorageAreaInfo()) {
      if (sa.anonymousReadEnabled()) {
        anonymousGrantedAuthorities.add(SAPermission.canRead(sa.name()));
      }
    }
  }

  protected void addSaGrantedAuthorities(StorageAreaInfo sa, String issuer) {
    if (sa.orgsGrantReadPermission()) {
      authzMap.put(issuer, SAPermission.canRead(sa.name()));
    }

    if (sa.orgsGrantWritePermission()) {
      authzMap.put(issuer, SAPermission.canWrite(sa.name()));
    }

  }

  protected Collection<GrantedAuthority> extractAuthoritiesExternalAuthzServer(String issuer) {
    return authzMap.get(issuer);
  }

}
