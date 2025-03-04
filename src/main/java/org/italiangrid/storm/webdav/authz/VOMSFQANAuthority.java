// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

public class VOMSFQANAuthority implements GrantedAuthority, Comparable<VOMSFQANAuthority> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @Override
  public int hashCode() {

    final int prime = 31;
    int result = 1;
    result = prime * result + ((fqanAuthority == null) ? 0 : fqanAuthority.hashCode());
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
    VOMSFQANAuthority other = (VOMSFQANAuthority) obj;
    if (fqanAuthority == null) {
      if (other.fqanAuthority != null)
        return false;
    } else if (!fqanAuthority.equals(other.fqanAuthority))
      return false;
    return true;
  }

  String fqanAuthority;

  public VOMSFQANAuthority(String fqan) {
    Assert.hasText(fqan, "FQAN must not be empty");
    fqanAuthority = String.format("FQAN(%s)", fqan);
  }

  @Override
  public int compareTo(VOMSFQANAuthority that) {

    return fqanAuthority.compareTo(that.fqanAuthority);
  }

  @Override
  public String getAuthority() {

    return fqanAuthority;
  }

  @Override
  public String toString() {

    return getAuthority();
  }

}
