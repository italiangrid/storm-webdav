/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
