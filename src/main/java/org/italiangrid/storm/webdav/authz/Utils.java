// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import java.security.cert.X509Certificate;
import java.util.Optional;

import javax.security.auth.x500.X500Principal;

import org.eclipse.jetty.ee10.servlet.ServletContextRequest;

import jakarta.servlet.http.HttpServletRequest;

import eu.emi.security.authn.x509.proxy.ProxyUtils;

public class Utils {

  private Utils() {

  }

  public static Optional<X509Certificate[]> getCertificateChainFromRequest(
      HttpServletRequest request) {

    X509Certificate[] chain =
        (X509Certificate[]) request.getAttribute(ServletContextRequest.PEER_CERTIFICATES);

    return Optional.ofNullable(chain);
  }

  public static Optional<X500Principal> getX500PrincipalFromRequest(HttpServletRequest request) {
    Optional<X509Certificate[]> chain = getCertificateChainFromRequest(request);

    if (chain.isPresent()) {
      return Optional.of(ProxyUtils.getOriginalUserDN(chain.get()));
    }

    return Optional.empty();
  }
}
