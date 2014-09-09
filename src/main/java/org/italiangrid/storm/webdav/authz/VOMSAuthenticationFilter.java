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

  public Object extractPrincipal(X509Certificate cert) {

    String[] cns = X500NameUtils.getAttributeValues(
      cert.getSubjectX500Principal(), CN_OID);

    for (String cn : cns) {

      if (cn.equals("proxy") || cn.matches("\\d+")) {
        logger.debug("Skipping CN={}", cn);
        continue;
      }

      return cn;
    }

    logger
      .warn("Failed to extract Principal from subject, will use the whole subject");
    return cert.getSubjectDN().getName();
  }

}
