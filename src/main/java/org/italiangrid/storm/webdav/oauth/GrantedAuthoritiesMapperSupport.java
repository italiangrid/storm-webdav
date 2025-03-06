// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.springframework.security.core.GrantedAuthority;

public class GrantedAuthoritiesMapperSupport {

  protected final Multimap<String, GrantedAuthority> authzMap = ArrayListMultimap.create();
  protected final AuthorizationServerProperties authzServerProperties;

  protected static final String[] OAUTH_GROUP_CLAIM_NAMES = {
    "groups", "wlcg.groups", "entitlements"
  };
  protected static final String SCOPE_CLAIM_NAME = "scope";

  protected final Set<GrantedAuthority> anonymousGrantedAuthorities = new HashSet<>();

  public GrantedAuthoritiesMapperSupport(
      StorageAreaConfiguration conf, ServiceConfigurationProperties props) {

    authzServerProperties = props.getAuthzServer();

    for (StorageAreaInfo sa : conf.getStorageAreaInfo()) {
      if (sa.orgs() != null) {
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
