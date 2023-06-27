/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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
