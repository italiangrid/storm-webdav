// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authority;

public class JwtClientAuthority extends JwtAuthority {

  private static final long serialVersionUID = 1L;

  public static final String AUTH_TEMPLATE = "O_client(%s,%s)";

  private final String clientId;

  public JwtClientAuthority(String issuer, String clientId) {
    super(issuer, String.format(AUTH_TEMPLATE, issuer, clientId));
    this.clientId = clientId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    JwtClientAuthority other = (JwtClientAuthority) obj;
    if (clientId == null) {
      if (other.clientId != null)
        return false;
    } else if (!clientId.equals(other.clientId))
      return false;
    return true;
  }

}
