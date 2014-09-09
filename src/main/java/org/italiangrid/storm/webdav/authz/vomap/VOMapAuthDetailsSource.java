package org.italiangrid.storm.webdav.authz.vomap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.AbstractVOMSAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSVOAuthority;
import org.springframework.security.core.GrantedAuthority;

public class VOMapAuthDetailsSource extends AbstractVOMSAuthDetailsSource {

  private final VOMapDetailsService mapService;

  public VOMapAuthDetailsSource(VOMapDetailsService mds) {

    mapService = mds;
  }

  @Override
  public Collection<GrantedAuthority> getVOMSGrantedAuthorities(
    HttpServletRequest request) {

    X500Principal principal = getPrincipalFromRequest(request);

    if (principal == null) {
      return Collections.emptyList();
    }

    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

    for (String voName : mapService.getPrincipalVOs(principal)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Adding VO authority: {}", voName);
      }
      authorities.add(new VOMSVOAuthority(voName));
    }

    return authorities;
  }

}
