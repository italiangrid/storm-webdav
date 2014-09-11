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

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.FormatMode;
import eu.emi.security.authn.x509.proxy.ProxyUtils;

public abstract class AbstractVOMSAuthDetailsSource implements
  VOMSAuthDetailsSource {

  public static final Logger logger = LoggerFactory
    .getLogger(AbstractVOMSAuthDetailsSource.class);

  protected AbstractVOMSAuthDetailsSource() {

  }

  protected X509Certificate[] getClientCertificateChain(
    HttpServletRequest request) {

    X509Certificate[] chain = Utils.getCertificateChainFromRequest(request);

    if (chain != null && chain.length > 0) {
      if (logger.isDebugEnabled()) {
        logger.debug("Certificate chain in incoming request: {}",
          CertificateUtils.format(chain, FormatMode.COMPACT_ONE_LINE));
      }
      return chain;
    }

    logger.debug("No certificate chain in incoming request.");
    return null;
  }

  protected X500Principal getPrincipalFromRequest(HttpServletRequest request) {

    X509Certificate[] chain = getClientCertificateChain(request);
    if (chain != null) {
      return ProxyUtils.getOriginalUserDN(chain);
    }
    return null;
  }
}
