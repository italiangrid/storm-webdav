// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authority;

public class JwtGroupAuthority extends JwtAuthority implements Comparable<JwtGroupAuthority> {

  private static final long serialVersionUID = 1L;

  public static final String AUTH_TEMPLATE = "O_g(%s,%s)";

  private final String group;

  public JwtGroupAuthority(String issuer, String group) {
    super(issuer, String.format(AUTH_TEMPLATE, issuer, group));
    this.group = group;
  }

  public String getGroup() {
    return group;
  }


  @Override
  public int compareTo(JwtGroupAuthority o) {
    if (o.getIssuer().equals(getIssuer())) {
      return group.compareTo(o.group);
    }

    return -1;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((group == null) ? 0 : group.hashCode());
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
    JwtGroupAuthority other = (JwtGroupAuthority) obj;
    if (group == null) {
      if (other.group != null)
        return false;
    } else if (!group.equals(other.group))
      return false;
    return true;
  }

}
