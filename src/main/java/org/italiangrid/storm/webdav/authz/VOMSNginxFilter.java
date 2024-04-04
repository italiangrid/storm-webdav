// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import javax.security.auth.x500.X500Principal;
import org.springframework.security.authentication.AuthenticationManager;

public class VOMSNginxFilter extends VOMSAuthenticationFilter {

  public VOMSNginxFilter(AuthenticationManager mgr) {
    super(mgr);
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(VOMSConstants.SSL_CLIENT_EE_S_DN_HEADER))
        .map(sslClientEeSDnHeader -> new X500Principal(sslClientEeSDnHeader).getName())
        .orElse(null);
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return new Object();
  }
}
