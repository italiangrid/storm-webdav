// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authority;

import org.springframework.security.core.GrantedAuthority;

public abstract class JwtAuthority implements GrantedAuthority {

  private static final long serialVersionUID = 1L;

  protected final String authority;
  protected final String issuer;
  
  protected JwtAuthority(String issuer, String authority) {
    this.issuer = issuer;
    this.authority = authority;
  }
  
  @Override
  public String getAuthority() {
    return authority;
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((issuer == null) ? 0 : issuer.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    JwtAuthority other = (JwtAuthority) obj;
    if (issuer == null) {
      if (other.issuer != null)
        return false;
    } else if (!issuer.equals(other.issuer))
      return false;
    return true;
  }

  

  public String getIssuer() {
    return issuer;
  }
  
  @Override
  public String toString() {
    return getAuthority();
  }
}
