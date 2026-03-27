// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumOutputStream;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.storm.webdav.tpc.http.GetResponseHandler;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetResponseHandlerTest extends ClientTestSupport {

  @Mock HttpEntity entity;

  @Mock ClassicHttpResponse response;

  @Mock Adler32ChecksumOutputStream os;

  GetResponseHandler handler;

  @Override
  @BeforeEach
  public void setup() {

    handler = new GetResponseHandler(req, os, eah);
    lenient().when(response.getEntity()).thenReturn(entity);
  }

  @Test
  void handlerWritesToStream() throws IOException {
    lenient().when(response.getFirstHeader(TransferConstants.REPR_DIGEST_HEADER)).thenReturn(null);
    handler.handleResponse(response);

    verify(entity).getContent();
    verify(eah).setChecksumAttribute(ArgumentMatchers.<Path>any(), any());
  }

  @Test
  void handlerErrorInCaseOfMismatchedChecksumFromPassive() {
    lenient().when(req.expectedChecksum()).thenReturn(Optional.of("045d01c1"));
    lenient()
        .when(response.getFirstHeader(TransferConstants.REPR_DIGEST_HEADER))
        .thenReturn(new BasicHeader(TransferConstants.REPR_DIGEST_HEADER, "adler=:MDNmYzAxOWQ=:"));

    ChecksumVerificationError checksumVerificationError =
        assertThrows(
            ChecksumVerificationError.class,
            () -> {
              handler.handleResponse(response);
            });
    assertEquals(
        "client/server checksum mismatch (checksum received from remote server mismatch)",
        checksumVerificationError.getMessage());
  }

  @Test
  void handlerErrorInCaseOfMismatchedChecksumFromStream() {
    lenient().when(req.expectedChecksum()).thenReturn(Optional.of("03fc019d"));
    lenient()
        .when(response.getFirstHeader(TransferConstants.REPR_DIGEST_HEADER))
        .thenReturn(new BasicHeader(TransferConstants.REPR_DIGEST_HEADER, "adler=:MDNmYzAxOWQ=:"));
    lenient().when(os.getChecksumValue()).thenReturn("045d01c1");

    ChecksumVerificationError checksumVerificationError =
        assertThrows(
            ChecksumVerificationError.class,
            () -> {
              handler.handleResponse(response);
            });
    assertEquals(
        "client/server checksum mismatch (checksum of the received file mismatch)",
        checksumVerificationError.getMessage());
  }

  @Test
  void handlerChecksumCorrect() throws IOException {
    lenient().when(req.expectedChecksum()).thenReturn(Optional.of("03fc019d"));
    lenient()
        .when(response.getFirstHeader(TransferConstants.REPR_DIGEST_HEADER))
        .thenReturn(new BasicHeader(TransferConstants.REPR_DIGEST_HEADER, "adler=:MDNmYzAxOWQ=:"));
    lenient().when(os.getChecksumValue()).thenReturn("03fc019d");
    handler.handleResponse(response);

    verify(entity).getContent();
    verify(eah).setChecksumAttribute(ArgumentMatchers.<Path>any(), any());
  }
}
