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

import static java.util.Objects.isNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumOutputStream;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetResponseHandler extends ResponseHandlerSupport
    implements org.apache.http.client.ResponseHandler<Boolean> {


  public static final int DEFAULT_BUFFER_SIZE = 4096;

  public static final Logger LOG = LoggerFactory.getLogger(GetResponseHandler.class);

  final GetTransferRequest request;
  final StormCountingOutputStream fileStream;
  final ExtendedAttributesHelper attributesHelper;
  final int bufferSize;
  final boolean computeChecksum;

  public GetResponseHandler(GetTransferRequest req, StormCountingOutputStream fs,
      ExtendedAttributesHelper ah, Map<String, String> mdcContextMap, int bufSiz,
      boolean computeChecksum) {

    super(mdcContextMap);
    request = req;
    fileStream = fs;
    attributesHelper = ah;
    bufferSize = bufSiz;
    this.computeChecksum = computeChecksum;
  }

  public GetResponseHandler(GetTransferRequest req, StormCountingOutputStream fs,
      ExtendedAttributesHelper ah) {
    this(req, fs, ah, Collections.emptyMap(), DEFAULT_BUFFER_SIZE, true);
  }

  private void writeEntityToStream(HttpEntity entity, OutputStream os)
      throws UnsupportedOperationException, IOException {

    final InputStream inStream = entity.getContent();

    if (!isNull(inStream)) {
      try {
        int l;
        final byte[] tmp = new byte[bufferSize];
        while ((l = inStream.read(tmp)) != -1) {
          os.write(tmp, 0, l);
        }
      } finally {
        inStream.close();
      }
    }
  }


  @Override
  public Boolean handleResponse(HttpResponse response) throws IOException {

    setupMDC();
    LOG.debug("Response: {}", response);

    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();

    checkResponseStatus(sl);

    Adler32ChecksumOutputStream checkedStream = null;

    OutputStream os = fileStream;

    if (computeChecksum) {
      checkedStream = new Adler32ChecksumOutputStream(fileStream);
      os = checkedStream;
    }

    try {

      if (entity != null) {

        writeEntityToStream(entity, os);
        if (computeChecksum) {
          attributesHelper.setChecksumAttribute(fileStream.getPath(),
              checkedStream.getChecksumValue());
        }
      }

      return true;

    } finally {
      fileStream.close();
      EntityUtils.consumeQuietly(entity);

    }

  }

}
