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
package org.italiangrid.storm.webdav.oauth.authority;

public class OAuthGroupAuthority extends OAuthAuthority implements Comparable<OAuthGroupAuthority> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static final String AUTH_TEMPLATE = "O_g(%s,%s)";

  private final String group;

  public OAuthGroupAuthority(String issuer, String group) {
    super(issuer);
    this.group = group;
  }

  public String getGroup() {
    return group;
  }

  @Override
  public String getAuthority() {
    return String.format(AUTH_TEMPLATE, issuer, group);
  }

  @Override
  public int compareTo(OAuthGroupAuthority o) {
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
    OAuthGroupAuthority other = (OAuthGroupAuthority) obj;
    if (group == null) {
      if (other.group != null)
        return false;
    } else if (!group.equals(other.group))
      return false;
    return true;
  }

}
