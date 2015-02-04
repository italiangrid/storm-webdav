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

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;

import eu.emi.security.authn.x509.helpers.JavaAndBCStyle;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.emi.security.authn.x509.proxy.ProxyUtils;

public class VOMSAuthenticationFilter extends X509AuthenticationFilter {

  public static final Logger logger = LoggerFactory
    .getLogger(VOMSAuthenticationFilter.class);

  private final JavaAndBCStyle BC_STYLE = new JavaAndBCStyle();
  private final ASN1ObjectIdentifier CN_OID = BC_STYLE.attrNameToOID("CN");

  public VOMSAuthenticationFilter(AuthenticationManager mgr) {

    setCheckForPrincipalChanges(true);
    setInvalidateSessionOnPrincipalChange(true);
    setContinueFilterChainOnUnsuccessfulAuthentication(true);
    setAuthenticationManager(mgr);
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

    X509Certificate[] chain = Utils.getCertificateChainFromRequest(request);

    if (chain != null) {

      return extractPrincipal(ProxyUtils.getEndUserCertificate(chain));

    }

    return null;
  }

  protected String extractCN(X509Certificate cert) {
    
    String[] cns = X500NameUtils.getAttributeValues(
      cert.getSubjectX500Principal(), CN_OID);

    for (String cn : cns) {

      if (cn.equals("proxy") || cn.matches("\\d+")) {
        logger.debug("Skipping CN={}", cn);
        continue;
      }

      return cn;
    }
    
    return null;
  }
  
  public Object extractPrincipal(X509Certificate cert) {
    
    return cert.getSubjectDN().getName();
  }

}
