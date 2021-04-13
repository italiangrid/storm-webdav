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
package org.italiangrid.storm.webdav.oauth.authority;

import org.springframework.security.core.GrantedAuthority;

public abstract class JwtAuthority implements GrantedAuthority {

  private static final long serialVersionUID = 1L;

  protected final String authority;
  protected final String issuer;
  
  protected JwtAuthority(String issuer, String authority) {
    this.issuer = issuer;
    this.authority = authority;
  }
  
  @Override
  public String getAuthority() {
    return authority;
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((issuer == null) ? 0 : issuer.hashCode());
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
    JwtAuthority other = (JwtAuthority) obj;
    if (issuer == null) {
      if (other.issuer != null)
        return false;
    } else if (!issuer.equals(other.issuer))
      return false;
    return true;
  }

  

  public String getIssuer() {
    return issuer;
  }
  
  @Override
  public String toString() {
    return getAuthority();
  }
}
