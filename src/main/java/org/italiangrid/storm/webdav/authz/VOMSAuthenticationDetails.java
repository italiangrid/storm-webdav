// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import org.italiangrid.voms.VOMSAttribute;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;

public class VOMSAuthenticationDetails
    extends PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails {

  private static final long serialVersionUID = 1L;

  // Transient here is due to the fact that VOMSAttributesImpl is not serializable!
  final transient List<VOMSAttribute> vomsAttributes;

  public VOMSAuthenticationDetails(
      HttpServletRequest request,
      Collection<? extends GrantedAuthority> authorities,
      List<VOMSAttribute> attributes) {
    super(request, authorities);
    this.vomsAttributes = attributes;
  }

  public List<VOMSAttribute> getVomsAttributes() {
    return vomsAttributes;
  }
}
