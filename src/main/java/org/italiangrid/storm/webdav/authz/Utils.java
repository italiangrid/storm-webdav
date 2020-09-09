/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import eu.emi.security.authn.x509.proxy.ProxyUtils;

public class Utils {

  public static final String X509_CERT_REQ_ATTR = "javax.servlet.request.X509Certificate";

  private Utils() {

  }

  public static Optional<X509Certificate[]> getCertificateChainFromRequest(
      HttpServletRequest request) {

    X509Certificate[] chain = (X509Certificate[]) request.getAttribute(X509_CERT_REQ_ATTR);

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
