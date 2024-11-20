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

import java.util.Map;

import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.client5.http.HttpResponseException;
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
