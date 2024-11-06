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
package org.italiangrid.storm.webdav.oauth;

import static org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer.CLAIM_AUTHORITIES;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.oauth.authority.JwtGroupAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtIssuerAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtScopeAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtSubjectAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component
public class StormJwtAuthoritiesConverter extends GrantedAuthoritiesMapperSupport
    implements Converter<Jwt, Collection<GrantedAuthority>> {

  @Autowired
  public StormJwtAuthoritiesConverter(StorageAreaConfiguration conf,
      ServiceConfigurationProperties props) {
    super(conf, props);
  }

  protected boolean isLocalAuthzServer(String issuer) {
    return issuer.equals(authzServerProperties.getIssuer());
  }

  protected Set<GrantedAuthority> extractAuthoritiesLocalAuthzServer(Jwt jwt) {
    Set<GrantedAuthority> authorities = Sets.newHashSet();

    Optional.ofNullable(
        jwt.getClaimAsStringList(CLAIM_AUTHORITIES))
      .ifPresent(a -> a.forEach(at -> authorities.add(SAPermission.fromString(at))));

    return authorities;
  }

  protected Set<GrantedAuthority> extractOauthScopeAuthorities(Jwt jwt) {

    Set<GrantedAuthority> scopeAuthorities = Sets.newHashSet();

    if (jwt.getClaimAsString(SCOPE_CLAIM_NAME) != null) {
      String tokenIssuer = jwt.getClaimAsString(JwtClaimNames.ISS);

      String[] scopes = jwt.getClaimAsString(SCOPE_CLAIM_NAME).split(" ");

      for (String s : scopes) {
        scopeAuthorities.add(new JwtScopeAuthority(tokenIssuer, s));
      }
    }

    return scopeAuthorities;
  }

  protected Set<GrantedAuthority> extractOauthGroupAuthorities(Jwt jwt) {

    Set<GrantedAuthority> groupAuthorities = Sets.newHashSet();

    String tokenIssuer = jwt.getClaimAsString(JwtClaimNames.ISS);

    for (String groupClaim : OAUTH_GROUP_CLAIM_NAMES) {
      if (Boolean.TRUE.equals(jwt.containsClaim(groupClaim))) {
        jwt.getClaimAsStringList(groupClaim)
          .forEach(gc -> groupAuthorities.add(new JwtGroupAuthority(tokenIssuer, gc)));
        break;
      }
    }
    return groupAuthorities;
  }

  protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

    String issuer = jwt.getIssuer().toString();

    if (isLocalAuthzServer(issuer)) {
      return extractAuthoritiesLocalAuthzServer(jwt);
    }

    Set<GrantedAuthority> authorities = Sets.newHashSet();

    authorities.addAll(extractAuthoritiesExternalAuthzServer(issuer));
    authorities.addAll(extractOauthGroupAuthorities(jwt));
    authorities.addAll(extractOauthScopeAuthorities(jwt));

    authorities.add(new JwtIssuerAuthority(jwt.getIssuer().toString()));
    authorities.add(new JwtSubjectAuthority(jwt.getIssuer().toString(), jwt.getSubject()));

    return authorities;
  }

  @Override
  public Collection<GrantedAuthority> convert(Jwt source) {
    return extractAuthorities(source);
  }

}
