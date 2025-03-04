// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth;

import static org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer.CLAIM_AUTHORITIES;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.oauth.authority.JwtClientAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtGroupAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtIssuerAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtScopeAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtSubjectAuthority;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.stereotype.Component;

@Component
public class StormJwtAuthoritiesConverter extends GrantedAuthoritiesMapperSupport
    implements Converter<Jwt, Collection<GrantedAuthority>> {

  public StormJwtAuthoritiesConverter(StorageAreaConfiguration conf,
      ServiceConfigurationProperties props) {
    super(conf, props);
  }

  protected boolean isLocalAuthzServer(String issuer) {
    return issuer.equals(authzServerProperties.getIssuer());
  }

  protected Set<GrantedAuthority> extractAuthoritiesLocalAuthzServer(Jwt jwt) {
    Set<GrantedAuthority> authorities = new HashSet<>();

    Optional.ofNullable(
        jwt.getClaimAsStringList(CLAIM_AUTHORITIES))
      .ifPresent(a -> a.forEach(at -> authorities.add(SAPermission.fromString(at))));

    return authorities;
  }

  protected Set<GrantedAuthority> extractOauthScopeAuthorities(Jwt jwt) {

    Set<GrantedAuthority> scopeAuthorities = new HashSet<>();

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

    Set<GrantedAuthority> groupAuthorities = new HashSet<>();

    String tokenIssuer = jwt.getClaimAsString(JwtClaimNames.ISS);

    for (String groupClaim : OAUTH_GROUP_CLAIM_NAMES) {
      if (jwt.hasClaim(groupClaim)) {
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

    Set<GrantedAuthority> authorities = new HashSet<>();

    authorities.addAll(extractAuthoritiesExternalAuthzServer(issuer));
    authorities.addAll(extractOauthGroupAuthorities(jwt));
    authorities.addAll(extractOauthScopeAuthorities(jwt));

    authorities.add(new JwtIssuerAuthority(issuer));
    authorities.add(new JwtSubjectAuthority(issuer, jwt.getSubject()));
    if (jwt.getClaim("client_id") != null) {
      authorities.add(new JwtClientAuthority(issuer, jwt.getClaim("client_id")));
    }

    return authorities;
  }

  @Override
  public Collection<GrantedAuthority> convert(Jwt source) {
    return extractAuthorities(source);
  }

}
