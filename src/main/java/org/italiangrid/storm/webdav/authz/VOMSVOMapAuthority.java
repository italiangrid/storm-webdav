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

import org.springframework.security.core.GrantedAuthority;

public class VOMSVOMapAuthority implements GrantedAuthority,
  Comparable<VOMSVOMapAuthority> {

  /**
   * 
   */
  private static final long serialVersionUID = -455904635960596363L;

  private final String voName;

  private final String authority;

  public VOMSVOMapAuthority(String vo) {

    authority = String.format("VO_MAP(%s)", vo);
    voName = vo;
  }

  @Override
  public String getAuthority() {

    return authority;
  }

  public String getVoName() {

    return voName;
  }

  @Override
  public int compareTo(VOMSVOMapAuthority o) {

    return authority.compareTo(o.authority);
  }

  @Override
  public String toString() {
    return authority;
  }
}
