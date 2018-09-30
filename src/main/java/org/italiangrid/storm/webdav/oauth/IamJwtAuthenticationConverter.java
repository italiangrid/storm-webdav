package org.italiangrid.storm.webdav.oauth;

import static java.util.Objects.isNull;

import java.util.Collection;

import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@Component
public class IamJwtAuthenticationConverter extends JwtAuthenticationConverter {

  final Multimap<String, GrantedAuthority> authzMap = ArrayListMultimap.create();

  protected void addSaGrantedAuthorities(String saName, String issuer, Boolean orgGrantsWriteAccess) {
    
    authzMap.put(issuer, SAPermission.canRead(saName));
    
    if (orgGrantsWriteAccess) {
      authzMap.put(issuer, SAPermission.canWrite(saName));
    }

  }

  @Autowired
  public IamJwtAuthenticationConverter(StorageAreaConfiguration conf) {

    for (StorageAreaInfo sa : conf.getStorageAreaInfo()) {
      if (!isNull(sa.orgs())) {
        sa.orgs().forEach(i -> addSaGrantedAuthorities(sa.name(), i, sa.orgsGrantWritePermission()));
      }
    }
  }


  @Override
  protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    
    String issuer = jwt.getIssuer().toString();
    Collection<GrantedAuthority> authorities = authzMap.get(issuer);
    
    return authorities;
  }


}
