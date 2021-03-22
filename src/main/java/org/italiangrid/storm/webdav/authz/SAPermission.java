/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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

import static java.lang.String.format;

import org.springframework.security.core.GrantedAuthority;

public class SAPermission implements GrantedAuthority, Comparable<SAPermission> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final String READ_PERM = "SA_READ(%s)";
  private static final String WRITE_PERM = "SA_WRITE(%s)";

  private final String authority;

  public static SAPermission canRead(String saName) {

    return new SAPermission(format(READ_PERM, saName));
  }

  public static SAPermission canWrite(String saName) {

    return new SAPermission(format(WRITE_PERM, saName));
  }

  private SAPermission(String permission) {

    authority = permission;
  }

  @Override
  public String getAuthority() {

    return authority;
  }

  @Override
  public String toString() {

    return authority;
  }

  @Override
  public int compareTo(SAPermission that) {

    return authority.compareTo(that.authority);

  }

  @Override
  public int hashCode() {

    final int prime = 31;
    int result = 1;
    result = prime * result + ((authority == null) ? 0 : authority.hashCode());
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
    SAPermission other = (SAPermission) obj;
    if (authority == null) {
      if (other.authority != null)
        return false;
    } else if (!authority.equals(other.authority))
      return false;
    return true;
  }

  public static SAPermission fromString(String s) {
    return new SAPermission(s);
  }
}
