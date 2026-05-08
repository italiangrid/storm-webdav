// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.milton.StoRMMiltonBehaviour;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PutRequestChecksumTest {

  public static final String SERVLET_PATH = "/test";
  public static final String LOCAL_PATH = "/some/file";

  public static final String FULL_LOCAL_PATH = SERVLET_PATH + LOCAL_PATH;

  @TempDir Path directory;

  @Mock Request request;

  @Mock ExtendedAttributesHelper eah;

  @Mock PathResolver resolver;

  StoRMMiltonBehaviour miltonBehaviour;

  @BeforeEach
  void setup() {
    miltonBehaviour = new StoRMMiltonBehaviour(eah, resolver, true);
    lenient().when(request.getAbsolutePath()).thenReturn(FULL_LOCAL_PATH);
  }

  @Test
  void reprDigestHeaderNotSended() throws IOException, BadRequestException {
    Path filePath = directory.resolve("tmpFile");
    Files.createFile(filePath);

    miltonBehaviour.putRequestHandling(request);

    verify(eah, never()).getChecksumAttribute(resolver.getPath(request.getAbsolutePath()));
    assertTrue(Files.exists(filePath));
  }

  @Test
  void checksumsMatchSoFileIsNotDeleted() throws IOException, BadRequestException {
    when(request.getHeaders())
        .thenReturn(
            Map.of(TransferConstants.REPR_DIGEST_HEADER.toLowerCase(), "adler=:MDNmYzAxOWQ:"));
    Path filePath = directory.resolve("tmpFile");
    Files.createFile(filePath);
    when(eah.getChecksumAttribute(filePath)).thenReturn("03fc019d");
    when(resolver.getPath(FULL_LOCAL_PATH)).thenReturn(filePath);

    miltonBehaviour.putRequestHandling(request);

    assertTrue(Files.exists(filePath));
  }

  @Test
  void checksumsMismatchSoFileIsDeleted() throws IOException {
    when(request.getHeaders())
        .thenReturn(
            Map.of(TransferConstants.REPR_DIGEST_HEADER.toLowerCase(), "adler=:MDNmYzAxOWQ:"));
    Path filePath = directory.resolve("tmpFile");
    Files.createFile(filePath);
    when(eah.getChecksumAttribute(filePath)).thenReturn("045d01c1");
    when(resolver.getPath(FULL_LOCAL_PATH)).thenReturn(filePath);

    ChecksumVerificationError checksumVerificationError =
        assertThrows(
            ChecksumVerificationError.class,
            () -> {
              miltonBehaviour.putRequestHandling(request);
            });

    assertEquals("client/server checksum mismatch", checksumVerificationError.getMessage());
    assertFalse(Files.exists(filePath));
  }

  @Test
  void checksumNotAvailableFailsButFileNotDeleted() throws IOException {
    when(request.getHeaders())
        .thenReturn(
            Map.of(TransferConstants.REPR_DIGEST_HEADER.toLowerCase(), "adler=:MDNmYzAxOWQ:"));
    Path filePath = directory.resolve("tmpFile");
    Files.createFile(filePath);
    Mockito.doThrow(new IOException(""))
        .when(eah)
        .getChecksumAttribute(ArgumentMatchers.<Path>any());
    when(resolver.getPath(FULL_LOCAL_PATH)).thenReturn(filePath);

    ChecksumVerificationError checksumVerificationError =
        assertThrows(
            ChecksumVerificationError.class,
            () -> {
              miltonBehaviour.putRequestHandling(request);
            });

    assertEquals(
        "Error retrieving checksum from the file system", checksumVerificationError.getMessage());
    assertTrue(Files.exists(filePath));
  }
}
