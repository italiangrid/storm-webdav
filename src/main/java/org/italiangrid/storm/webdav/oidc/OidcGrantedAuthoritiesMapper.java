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
package org.italiangrid.storm.webdav.oidc;

import static java.util.Objects.isNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.oauth.GrantedAuthoritiesMapperSupport;
import org.italiangrid.storm.webdav.oauth.authority.JwtGroupAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtSubjectAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component
public class OidcGrantedAuthoritiesMapper extends GrantedAuthoritiesMapperSupport
    implements GrantedAuthoritiesMapper {

  @Autowired
  public OidcGrantedAuthoritiesMapper(StorageAreaConfiguration conf,
      ServiceConfigurationProperties props) {
    super(conf, props);
  }

  protected Collection<GrantedAuthority> grantGroupAuthorities(OidcUserAuthority userAuthority) {
    Set<GrantedAuthority> groupAuthorities = Sets.newHashSet();
    String idTokenIssuer = userAuthority.getIdToken().getIssuer().toString();
    
    for (String groupClaimName : OAUTH_GROUP_CLAIM_NAMES) {
      List<String> groups = userAuthority.getIdToken().getClaimAsStringList(groupClaimName);
      if (!isNull(groups)) {
        groups.stream().map(g->new JwtGroupAuthority(idTokenIssuer, g)).forEach(groupAuthorities::add);
        break;
      }
    }
    
    return groupAuthorities;
  }

  protected Collection<GrantedAuthority> mapAuthorities(OidcUserAuthority userAuthority) {
    Set<GrantedAuthority> authorities = Sets.newHashSet();
    String idTokenIssuer = userAuthority.getIdToken().getIssuer().toString();
    
    authorities.addAll(authzMap.get(idTokenIssuer));
    authorities.addAll(grantGroupAuthorities(userAuthority));
    authorities.add(new JwtSubjectAuthority(userAuthority.getIdToken().getIssuer().toString(), 
        userAuthority.getIdToken().getSubject()));
    
    return authorities;
  }

  @Override
  public Collection<? extends GrantedAuthority> mapAuthorities(
      Collection<? extends GrantedAuthority> authorities) {

    Set<GrantedAuthority> grantedAuthorities = Sets.newHashSet();

    authorities.stream()
      .filter(OidcUserAuthority.class::isInstance)
      .map(OidcUserAuthority.class::cast)
      .map(this::mapAuthorities)
      .forEach(grantedAuthorities::addAll);

    grantedAuthorities.addAll(anonymousGrantedAuthorities);
    
    return grantedAuthorities;
  }

}
