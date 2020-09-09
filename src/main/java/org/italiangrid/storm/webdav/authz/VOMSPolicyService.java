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

import static java.util.Objects.isNull;
import static org.italiangrid.storm.webdav.authz.SAPermission.canRead;
import static org.italiangrid.storm.webdav.authz.SAPermission.canWrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@Service
public class VOMSPolicyService implements AuthorizationPolicyService {

  private final List<SAPermission> authenticatedPerms;
  private final Multimap<String, SAPermission> voPerms;
  private final Multimap<String, SAPermission> voMapPerms;

  public VOMSPolicyService(StorageAreaConfiguration saConfig) {
    authenticatedPerms = new ArrayList<SAPermission>();

    voPerms = ArrayListMultimap.create();
    voMapPerms = ArrayListMultimap.create();

    for (StorageAreaInfo sa : saConfig.getStorageAreaInfo()) {
      if (!isNull(sa.vos())) {
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

  @Override
  public Set<GrantedAuthority> getSAPermissions(
      Collection<? extends GrantedAuthority> authorities) {

    Set<GrantedAuthority> saPermissions = Sets.newHashSet();

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

  @Override
  public Set<GrantedAuthority> getSAPermissions(Authentication authn) {
    Preconditions.checkNotNull(authn);
    return getSAPermissions(authn.getAuthorities());
  }

}
