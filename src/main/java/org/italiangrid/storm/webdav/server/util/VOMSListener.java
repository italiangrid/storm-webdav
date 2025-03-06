// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.util;

import java.io.File;
import java.security.cert.X509Certificate;
import org.italiangrid.voms.ac.VOMSValidationResult;
import org.italiangrid.voms.ac.ValidationResultListener;
import org.italiangrid.voms.store.LSCInfo;
import org.italiangrid.voms.store.VOMSTrustStoreStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VOMSListener implements ValidationResultListener, VOMSTrustStoreStatusListener {

  public static final Logger LOG = LoggerFactory.getLogger(VOMSListener.class);

  public VOMSListener() {}

  @Override
  public void notifyValidationResult(VOMSValidationResult result) {
    if (!result.isValid()) {
      LOG.warn("VOMS attributes validation failed: {}", result);
    } else {
      LOG.trace("VOMS attributes validation success: {}", result);
    }
  }

  @Override
  public void notifyCertficateLookupEvent(String dir) {}

  @Override
  public void notifyLSCLookupEvent(String dir) {
    LOG.debug("Looking for LSC files in {}", dir);
  }

  @Override
  public void notifyCertificateLoadEvent(X509Certificate cert, File f) {}

  @Override
  public void notifyLSCLoadEvent(LSCInfo lsc, File f) {
    LOG.debug("Loaded LSC {} from {}", lsc, f.getPath());
  }
}
