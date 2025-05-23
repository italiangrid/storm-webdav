// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.util;

import eu.emi.security.authn.x509.StoreUpdateListener;
import eu.emi.security.authn.x509.ValidationError;
import eu.emi.security.authn.x509.ValidationErrorListener;
import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.FormatMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CANLListener implements StoreUpdateListener, ValidationErrorListener {

  public static final Logger LOG = LoggerFactory.getLogger(CANLListener.class);

  @Override
  public void loadingNotification(String location, String type, Severity level, Exception cause) {

    if (location.startsWith("file:")) {
      location = location.substring(5, location.length());
    }

    if (level.equals(Severity.ERROR)) {
      LOG.error("Error for {} {}: {}.", type, location, cause.getMessage());

    } else if (level.equals(Severity.WARNING)) {
      LOG.debug("Warning for {} {}: {}.", type, location, cause.getMessage());

    } else if (level.equals(Severity.NOTIFICATION)) {
      LOG.debug("Loading {} {}.", type, location);
    }
  }

  @Override
  public boolean onValidationError(ValidationError error) {

    String certChainInfo = CertificateUtils.format(error.getChain(), FormatMode.COMPACT_ONE_LINE);
    LOG.warn(
        "Certificate validation error for chain: {}. Error: {}", certChainInfo, error.getMessage());
    return false;
  }
}
