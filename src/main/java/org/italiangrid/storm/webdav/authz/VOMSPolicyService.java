// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import static org.italiangrid.storm.webdav.authz.SAPermission.canRead;
import static org.italiangrid.storm.webdav.authz.SAPermission.canWrite;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class VOMSPolicyService implements AuthorizationPolicyService {

  private final List<SAPermission> authenticatedPerms;
  private final Multimap<String, SAPermission> voPerms;
  private final Multimap<String, SAPermission> voMapPerms;

  @WithSpan
  public VOMSPolicyService(StorageAreaConfiguration saConfig) {
    authenticatedPerms = new ArrayList<>();

    voPerms = ArrayListMultimap.create();
    voMapPerms = ArrayListMultimap.create();

    for (StorageAreaInfo sa : saConfig.getStorageAreaInfo()) {
      if (sa.vos() != null) {
        for (String vo : sa.vos()) {
          voPerms.put(vo, canRead(sa.name()));
          voPerms.put(vo, canWrite(sa.name()));
          if (sa.voMapEnabled()) {
            voMapPerms.put(vo, canRead(sa.name()));
            if (sa.voMapGrantsWritePermission()) {
              voMapPerms.put(vo, canWrite(sa.name()));
            }
          }
        }
      }

      if (sa.authenticatedReadEnabled() || sa.anonymousReadEnabled()) {
        authenticatedPerms.add(SAPermission.canRead(sa.name()));
      }
    }
  }

  @WithSpan
  @Override
  public Set<GrantedAuthority> getSAPermissions(
      Collection<? extends GrantedAuthority> authorities) {

    Set<GrantedAuthority> saPermissions = new HashSet<>();

    authorities.stream()
        .filter(VOMSVOAuthority.class::isInstance)
        .map(VOMSVOAuthority.class::cast)
        .forEach(a -> saPermissions.addAll(voPerms.get(a.getVoName())));

    authorities.stream()
        .filter(VOMSVOMapAuthority.class::isInstance)
        .map(VOMSVOMapAuthority.class::cast)
        .forEach(a -> saPermissions.addAll(voMapPerms.get(a.getVoName())));

    saPermissions.addAll(authenticatedPerms);
    return saPermissions;
  }

  @WithSpan
  @Override
  public Set<GrantedAuthority> getSAPermissions(Authentication authn) {
    Objects.requireNonNull(authn);
    return getSAPermissions(authn.getAuthorities());
  }
}
