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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;

public class VOMSPreAuthDetailsSource
  implements
  AuthenticationDetailsSource<HttpServletRequest, PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails> {

  public final List<VOMSAuthDetailsSource> vomsAuthoritiesSources;

  public VOMSPreAuthDetailsSource(List<VOMSAuthDetailsSource> vas) {

    this.vomsAuthoritiesSources = vas;
  }

  @Override
  public PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails buildDetails(
    HttpServletRequest request) {

    return new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(
      request, getVOMSGrantedAuthorities(request));
  }

  private Collection<? extends GrantedAuthority> getVOMSGrantedAuthorities(
    HttpServletRequest request) {

    Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

    for (VOMSAuthDetailsSource source : vomsAuthoritiesSources) {
      authorities.addAll(source.getVOMSGrantedAuthorities(request));
    }

    return Collections.unmodifiableSet(authorities);
  }
}
