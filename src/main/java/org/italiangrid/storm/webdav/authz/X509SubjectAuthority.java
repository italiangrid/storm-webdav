// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import org.springframework.security.core.GrantedAuthority;

public class X509SubjectAuthority implements GrantedAuthority, Comparable<X509SubjectAuthority> {

  private static final long serialVersionUID = 1L;

  public static final String AUTHORITY_TEMPLATE = "X509_sub(%s)";

  final String authority;

  public X509SubjectAuthority(String subject) {
    this.authority = String.format(AUTHORITY_TEMPLATE, subject);
  }

  @Override
  public int compareTo(X509SubjectAuthority o) {
    return authority.compareTo(o.authority);
  }

  @Override
  public String getAuthority() {

    return authority;
  }

  @Override
  public String toString() {
    return authority;
  }

}
