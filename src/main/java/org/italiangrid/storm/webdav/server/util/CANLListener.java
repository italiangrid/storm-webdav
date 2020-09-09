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
package org.italiangrid.storm.webdav.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.emi.security.authn.x509.StoreUpdateListener;
import eu.emi.security.authn.x509.ValidationError;
import eu.emi.security.authn.x509.ValidationErrorListener;
import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.FormatMode;

public class CANLListener implements StoreUpdateListener, ValidationErrorListener {

  public static final Logger LOG = LoggerFactory.getLogger(CANLListener.class);

  @Override
  public void loadingNotification(String location, String type, Severity level, Exception cause) {

    if (location.startsWith("file:"))
      location = location.substring(5, location.length());

    if (level.equals(Severity.ERROR)) {
      LOG.error("Error for {} {}: {}.", new Object[] {type, location, cause.getMessage()});

    } else if (level.equals(Severity.WARNING)) {
      LOG.debug("Warning for {} {}: {}.", new Object[] {type, location, cause.getMessage()});

    } else if (level.equals(Severity.NOTIFICATION)) {
      LOG.debug("Loading {} {}.", new Object[] {type, location});
    }
  }

  @Override
  public boolean onValidationError(ValidationError error) {

    String certChainInfo = CertificateUtils.format(error.getChain(), FormatMode.COMPACT_ONE_LINE);
    LOG.warn("Certificate validation error for chain: {}. Error: {}", certChainInfo,
        error.getMessage());
    return false;

  }

}
