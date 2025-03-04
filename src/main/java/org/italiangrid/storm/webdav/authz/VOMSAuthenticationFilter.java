// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import java.security.cert.X509Certificate;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;

import eu.emi.security.authn.x509.proxy.ProxyUtils;

public class VOMSAuthenticationFilter extends X509AuthenticationFilter {

  public VOMSAuthenticationFilter(AuthenticationManager mgr) {
    setCheckForPrincipalChanges(false);
    setInvalidateSessionOnPrincipalChange(false);
    setContinueFilterChainOnUnsuccessfulAuthentication(true);
    setAuthenticationManager(mgr);
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

    Optional<X509Certificate[]> chain = Utils.getCertificateChainFromRequest(request);

    if (chain.isPresent()) {
      return extractPrincipal(ProxyUtils.getEndUserCertificate(chain.get()));
    }

    return null;
  }


  public Object extractPrincipal(X509Certificate cert) {

    return cert.getSubjectX500Principal().getName();
  }

  @Override
  public boolean principalChanged(HttpServletRequest request,
      Authentication currentAuthentication) {
    return super.principalChanged(request, currentAuthentication);
  }

}
