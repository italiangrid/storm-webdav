package org.italiangrid.storm.webdav.oauth;

import static org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer.CLAIM_AUTHORITIES;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.oauth.authority.OAuthGroupAuthority;
import org.italiangrid.storm.webdav.oauth.authority.OAuthScopeAuthority;
import org.italiangrid.storm.webdav.oidc.authority.OidcSubjectAuthority;
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

    jwt.getClaimAsStringList(CLAIM_AUTHORITIES)
      .forEach(a -> authorities.add(SAPermission.fromString(a)));

    return authorities;
  }

  protected Set<GrantedAuthority> extractOauthScopeAuthorities(Jwt jwt) {

    Set<GrantedAuthority> scopeAuthorities = Sets.newHashSet();

    if (!Objects.isNull(jwt.getClaimAsString(SCOPE_CLAIM_NAME))) {
      String tokenIssuer = jwt.getClaimAsString(JwtClaimNames.ISS);

      String[] scopes = jwt.getClaimAsString(SCOPE_CLAIM_NAME).split(" ");

      for (String s : scopes) {
        scopeAuthorities.add(new OAuthScopeAuthority(tokenIssuer, s));
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
          .forEach(gc -> groupAuthorities.add(new OAuthGroupAuthority(tokenIssuer, gc)));
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

    authorities.add(new OidcSubjectAuthority(jwt.getIssuer().toString(), jwt.getSubject()));

    return authorities;
  }

  @Override
  public Collection<GrantedAuthority> convert(Jwt source) {
    return extractAuthorities(source);
  }

}
