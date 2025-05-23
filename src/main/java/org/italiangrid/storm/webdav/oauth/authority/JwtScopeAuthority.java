// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authority;

public class JwtScopeAuthority extends JwtAuthority implements Comparable<JwtScopeAuthority> {

  private static final long serialVersionUID = 1L;

  final String scope;

  public static final String AUTH_TEMPLATE = "O_s(%s,%s)";

  public JwtScopeAuthority(String issuer, String scope) {
    super(issuer, String.format(AUTH_TEMPLATE, issuer, scope));
    this.scope = scope;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((scope == null) ? 0 : scope.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!super.equals(obj)) {
      return false;
    } else if (getClass() != obj.getClass()) {
      return false;
    }
    JwtScopeAuthority other = (JwtScopeAuthority) obj;
    if (scope == null) {
      if (other.scope != null) {
        return false;
      }
    } else if (!scope.equals(other.scope)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(JwtScopeAuthority o) {
    if (o.getIssuer().equals(getIssuer())) {
      return scope.compareTo(o.scope);
    }

    return -1;
  }

  public String getScope() {
    return scope;
  }
}
