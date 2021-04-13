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

import org.springframework.security.core.GrantedAuthority;

public class VOMSVOAuthority implements GrantedAuthority,
  Comparable<VOMSVOAuthority> {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  private final String voAuthority;

  private final String voName;

  public VOMSVOAuthority(String vo) {

    this.voAuthority = String.format("VO(%s)", vo);
    this.voName = vo;
  }

  @Override
  public String getAuthority() {

    return voAuthority;
  }

  public String getVoName() {

    return voName;
  }

  @Override
  public int compareTo(VOMSVOAuthority that) {

    return voAuthority.compareTo(that.getAuthority());
  }

  @Override
  public int hashCode() {

    final int prime = 31;
    int result = 1;
    result = prime * result
      + ((voAuthority == null) ? 0 : voAuthority.hashCode());
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
    VOMSVOAuthority other = (VOMSVOAuthority) obj;
    if (voAuthority == null) {
      if (other.voAuthority != null)
        return false;
    } else if (!voAuthority.equals(other.voAuthority))
      return false;
    return true;
  }

  @Override
  public String toString() {

    return getAuthority();
  }
}
