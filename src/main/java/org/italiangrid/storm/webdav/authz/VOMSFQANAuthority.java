/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import org.springframework.security.core.GrantedAuthority;

public class VOMSFQANAuthority implements GrantedAuthority, Comparable<VOMSFQANAuthority> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Override
  public int hashCode() {

    final int prime = 31;
    int result = 1;
    result = prime * result + ((fqanAuthority == null) ? 0 : fqanAuthority.hashCode());
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
    VOMSFQANAuthority other = (VOMSFQANAuthority) obj;
    if (fqanAuthority == null) {
      if (other.fqanAuthority != null)
        return false;
    } else if (!fqanAuthority.equals(other.fqanAuthority))
      return false;
    return true;
  }

  String fqanAuthority;

  public VOMSFQANAuthority(String fqan) {
    checkArgument(!isNullOrEmpty(fqan));
    fqanAuthority = String.format("FQAN(%s)", fqan);
  }

  @Override
  public int compareTo(VOMSFQANAuthority that) {

    return fqanAuthority.compareTo(that.fqanAuthority);
  }

  @Override
  public String getAuthority() {

    return fqanAuthority;
  }

  @Override
  public String toString() {

    return getAuthority();
  }

}
