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

import java.security.cert.X509Certificate;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

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

    return cert.getSubjectDN().getName();
  }
  
  @Override
  public boolean principalChanged(HttpServletRequest request,
      Authentication currentAuthentication) {
    return super.principalChanged(request, currentAuthentication);
  }

}
