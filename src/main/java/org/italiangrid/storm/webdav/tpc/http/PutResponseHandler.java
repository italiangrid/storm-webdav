// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.http;

import java.io.IOException;
import java.util.Map;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class PutResponseHandler extends ResponseHandlerSupport
    implements HttpClientResponseHandler<Boolean> {

  public static final Logger LOG = LoggerFactory.getLogger(PutResponseHandler.class);

  public PutResponseHandler(Map<String, String> mdcContextMap) {
    super(mdcContextMap);
  }

  @Override
  public Boolean handleResponse(ClassicHttpResponse response) throws IOException {
    setupMDC();

    try {
      checkResponseStatus(response);
      return true;
    } finally {
      EntityUtils.consumeQuietly(response.getEntity());
      MDC.clear();
    }
  }
}
