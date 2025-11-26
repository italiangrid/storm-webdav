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
import java.util.Optional;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumOutputStream;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.italiangrid.storm.webdav.tpc.utils.Adler32DigestHeaderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetResponseHandler extends ResponseHandlerSupport
    implements HttpClientResponseHandler<Boolean> {

  public static final int DEFAULT_BUFFER_SIZE = 4096;

  public static final Logger LOG = LoggerFactory.getLogger(GetResponseHandler.class);

  final GetTransferRequest request;
  final Adler32ChecksumOutputStream fileStream;
  final ExtendedAttributesHelper attributesHelper;
  final int bufferSize;
  final boolean tapeEnabledStorageArea;
  final ApacheHttpClientContext observationContext;

  public GetResponseHandler(
      GetTransferRequest req,
      Adler32ChecksumOutputStream fs,
      ExtendedAttributesHelper ah,
      Map<String, String> mdcContextMap,
      int bufSiz,
      boolean tapeEnabledStorageArea,
      ApacheHttpClientContext observationContext) {

    super(mdcContextMap);
    request = req;
    fileStream = fs;
    attributesHelper = ah;
    bufferSize = bufSiz;
    this.tapeEnabledStorageArea = tapeEnabledStorageArea;
    this.observationContext = observationContext;
  }

  public GetResponseHandler(
      GetTransferRequest req, Adler32ChecksumOutputStream fs, ExtendedAttributesHelper ah) {
    this(req, fs, ah, Collections.emptyMap(), DEFAULT_BUFFER_SIZE, false, null);
  }

  private void writeEntityToStream(HttpEntity entity, OutputStream os)
      throws UnsupportedOperationException, IOException {

    try (InputStream inStream = entity.getContent()) {
      if (inStream != null) {
        int l;
        final byte[] tmp = new byte[bufferSize];
        while ((l = inStream.read(tmp)) != -1) {
          os.write(tmp, 0, l);
        }
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

    try {

      if (entity != null) {

        // If the PASSIVE site answers with a digest that does not match the one provided by the
        // client, the ACTIVE site can short-cut and avoid performing a digest computation on the
        // data and fail the transfer.
        Optional.ofNullable(response.getFirstHeader(TransferConstants.REPR_DIGEST_HEADER))
            .map(Header::getValue)
            .flatMap(Adler32DigestHeaderHelper::extractAdler32DigestFromHeaderValue)
            .ifPresent(
                passiveSiteChecksum ->
                    request
                        .expectedChecksum()
                        .ifPresent(
                            expectedChecksum -> {
                              if (!expectedChecksum.equals(passiveSiteChecksum)) {
                                throw new ChecksumVerificationError(
                                    "client/server checksum mismatch (checksum received from remote server mismatch)");
                              }
                            }));

        writeEntityToStream(entity, fileStream);

        String checksum = fileStream.getChecksumValue();
        // The ACTIVE site MUST compute a digest based on the received data from the PASSIVE site
        // and check it against the one provided by the client. This is necessary to avoid
        // corruption during traffic.
        request
            .expectedChecksum()
            .ifPresent(
                expectedChecksum -> {
                  if (!expectedChecksum.equals(checksum)) {
                    throw new ChecksumVerificationError(
                        "client/server checksum mismatch (checksum of the received file mismatch)");
                  }
                });
        attributesHelper.setChecksumAttribute(fileStream.getPath(), checksum);
        if (tapeEnabledStorageArea) {
          try {
            attributesHelper.setPremigrateAttribute(fileStream.getPath());
          } catch (IOException e) {
            LOG.warn(
                "Error setting premigrate extended attribute to {}: {}",
                fileStream.getPath(),
                e.getMessage(),
                e);
          }
        }
      }

      return true;

    } finally {
      EntityUtils.consumeQuietly(entity);
    }
  }
}
