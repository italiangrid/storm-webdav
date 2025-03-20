// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.http;

import io.micrometer.core.instrument.binder.httpcomponents.hc5.ApacheHttpClientContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumOutputStream;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetResponseHandler extends ResponseHandlerSupport
    implements HttpClientResponseHandler<Boolean> {

  public static final int DEFAULT_BUFFER_SIZE = 4096;

  public static final Logger LOG = LoggerFactory.getLogger(GetResponseHandler.class);

  final GetTransferRequest request;
  final StormCountingOutputStream fileStream;
  final ExtendedAttributesHelper attributesHelper;
  final int bufferSize;
  final boolean computeChecksum;
  final ApacheHttpClientContext observationContext;

  public GetResponseHandler(
      GetTransferRequest req,
      StormCountingOutputStream fs,
      ExtendedAttributesHelper ah,
      Map<String, String> mdcContextMap,
      int bufSiz,
      boolean computeChecksum,
      ApacheHttpClientContext observationContext) {

    super(mdcContextMap);
    request = req;
    fileStream = fs;
    attributesHelper = ah;
    bufferSize = bufSiz;
    this.computeChecksum = computeChecksum;
    this.observationContext = observationContext;
  }

  public GetResponseHandler(
      GetTransferRequest req, StormCountingOutputStream fs, ExtendedAttributesHelper ah) {
    this(req, fs, ah, Collections.emptyMap(), DEFAULT_BUFFER_SIZE, true, null);
  }

  private void writeEntityToStream(HttpEntity entity, OutputStream os)
      throws UnsupportedOperationException, IOException {

    final InputStream inStream = entity.getContent();

    if (inStream != null) {
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
  public Boolean handleResponse(ClassicHttpResponse response) throws IOException {

    setupMDC();
    LOG.debug("Response: {}", response);

    if (this.observationContext != null) {
      this.observationContext.setResponse(response);
    }

    HttpEntity entity = response.getEntity();

    checkResponseStatus(response);

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
          attributesHelper.setChecksumAttribute(
              fileStream.getPath(), checkedStream.getChecksumValue());
        }
      }

      return true;

    } finally {
      fileStream.close();
      EntityUtils.consumeQuietly(entity);
    }
  }
}
