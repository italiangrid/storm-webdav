/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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
package org.italiangrid.storm.webdav.tpc.http;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class PutResponseHandler extends ResponseHandlerSupport implements ResponseHandler<Boolean> {

  public static final Logger LOG = LoggerFactory.getLogger(PutResponseHandler.class);

  public PutResponseHandler(Map<String, String> mdcContextMap) {
    super(mdcContextMap);
  }

  @Override
  public Boolean handleResponse(HttpResponse response) throws IOException {
    setupMDC();
    
    try {
      checkResponseStatus(response.getStatusLine());
      return true;
    } finally {
      EntityUtils.consumeQuietly(response.getEntity());
      MDC.clear();
    }
  }
}
