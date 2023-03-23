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
package org.italiangrid.storm.webdav.authz;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.voms.VOMSAttribute;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;

public class VOMSAuthenticationDetails
    extends PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails {
  
  private static final long serialVersionUID = 1L;
    
  // Transient here is due to the fact that VOMSAttributesImpl is not serializable!
  final transient List<VOMSAttribute> vomsAttributes;

  public VOMSAuthenticationDetails(HttpServletRequest request,
      Collection<? extends GrantedAuthority> authorities, List<VOMSAttribute> attributes) {
    super(request, authorities);
    this.vomsAttributes = attributes;
  }

  public List<VOMSAttribute> getVomsAttributes() {
    return vomsAttributes;
  }

}
