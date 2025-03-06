// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.http;

import java.util.Map;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.HttpResponse;
import org.slf4j.MDC;

public abstract class ResponseHandlerSupport {

  final Map<String, String> mdcContextMap;

  protected ResponseHandlerSupport(Map<String, String> mdcContextMap) {
    this.mdcContextMap = mdcContextMap;
  }

  protected void setupMDC() {
    if (mdcContextMap != null) {
      MDC.setContextMap(mdcContextMap);
    }
  }

  protected void checkResponseStatus(HttpResponse response) throws HttpResponseException {
    if (response.getCode() >= 300) {
      throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
    }
  }
}
