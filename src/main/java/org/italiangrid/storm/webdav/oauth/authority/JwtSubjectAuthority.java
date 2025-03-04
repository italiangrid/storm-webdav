// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authority;

public class JwtSubjectAuthority extends JwtAuthority {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static final String AUTH_TEMPLATE = "O_sub(%s,%s)";

  private final String subject;

  public JwtSubjectAuthority(String issuer, String subject) {
    super(issuer, String.format(AUTH_TEMPLATE, issuer, subject));
    this.subject = subject;
  }

  public String getSubject() {
    return subject;
  }
}
