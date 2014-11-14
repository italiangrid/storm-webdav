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
package org.italiangrid.storm.webdav.authz.vomap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.AbstractVOMSAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSVOMapAuthority;
import org.springframework.security.core.GrantedAuthority;

public class VOMapAuthDetailsSource extends AbstractVOMSAuthDetailsSource {

  private final VOMapDetailsService mapService;

  public VOMapAuthDetailsSource(VOMapDetailsService mds) {

    mapService = mds;
  }

  @Override
  public Collection<GrantedAuthority> getVOMSGrantedAuthorities(
    HttpServletRequest request) {

    X500Principal principal = getPrincipalFromRequest(request);

    if (principal == null) {
      return Collections.emptyList();
    }

    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

    for (String voName : mapService.getPrincipalVOs(principal)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Adding VO authority: {}", voName);
      }
      authorities.add(new VOMSVOMapAuthority(voName));
    }

    return authorities;
  }

}
