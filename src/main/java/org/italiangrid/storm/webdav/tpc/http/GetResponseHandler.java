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
package org.italiangrid.storm.webdav.tpc.http;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumOutputStream;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class GetResponseHandler extends ResponseHandlerSupport
    implements org.apache.http.client.ResponseHandler<Boolean> {

  public static final Logger LOG = LoggerFactory.getLogger(GetResponseHandler.class);

  final GetTransferRequest request;
  final StormCountingOutputStream fileStream;
  final ExtendedAttributesHelper attributesHelper;

  public GetResponseHandler(GetTransferRequest req, StormCountingOutputStream fs,
      ExtendedAttributesHelper ah, Map<String, String> mdcContextMap) {

    super(mdcContextMap);
    request = req;
    fileStream = fs;
    attributesHelper = ah;
  }

  public GetResponseHandler(GetTransferRequest req, StormCountingOutputStream fs,
      ExtendedAttributesHelper ah) {
    this(req, fs, ah, Collections.emptyMap());
  }

  @Override
  public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

    setupMDC();
    LOG.debug("Response: {}", response);

    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();

    checkResponseStatus(sl);

    Adler32ChecksumOutputStream checkedStream = new Adler32ChecksumOutputStream(fileStream);

    try {

      if (entity != null) {

        entity.writeTo(checkedStream);
        attributesHelper.setChecksumAttribute(fileStream.getPath(),
            checkedStream.getChecksumValue());
      }

      return true;

    } finally {
      fileStream.close();
      EntityUtils.consumeQuietly(entity);
      MDC.clear();
    }

  }

}
