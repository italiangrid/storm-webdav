/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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

import java.io.File;
import java.security.cert.X509Certificate;

import org.italiangrid.voms.ac.VOMSValidationResult;
import org.italiangrid.voms.ac.ValidationResultListener;
import org.italiangrid.voms.store.LSCInfo;
import org.italiangrid.voms.store.VOMSTrustStoreStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VOMSListener
    implements ValidationResultListener, VOMSTrustStoreStatusListener {

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
