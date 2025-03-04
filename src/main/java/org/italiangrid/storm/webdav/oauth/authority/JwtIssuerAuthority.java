// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authority;

public class JwtIssuerAuthority extends JwtAuthority implements Comparable<JwtIssuerAuthority> {

  private static final long serialVersionUID = 1L;

  public static final String AUTH_TEMPLATE = "O_i(%s)";

  public JwtIssuerAuthority(String issuer) {
    super(issuer, String.format(AUTH_TEMPLATE, issuer));
  }

  @Override
  public int compareTo(JwtIssuerAuthority o) {
    return getIssuer().compareTo(o.getIssuer());
  }
}
