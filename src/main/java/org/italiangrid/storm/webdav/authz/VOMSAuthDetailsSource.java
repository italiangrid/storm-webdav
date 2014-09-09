package org.italiangrid.storm.webdav.authz;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.GrantedAuthority;

public interface VOMSAuthDetailsSource {

  public Collection<GrantedAuthority> getVOMSGrantedAuthorities(
    HttpServletRequest request);

}
