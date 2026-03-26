// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.checksum;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.milton.http.Request;
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
  void reprDigestHeaderNotSended() throws IOException {
    Path filePath = directory.resolve("tmpFile");
    Files.createFile(filePath);

    miltonBehaviour.putRequestHandling(request);

    verify(resolver, never()).getPath(request.getAbsolutePath());
    assertThat(Files.exists(filePath), is(true));
  }

  @Test
  void checksumsMatchSoFileIsNotDeleted() throws IOException {
    when(request.getHeaders())
        .thenReturn(
            Map.of(TransferConstants.REPR_DIGEST_HEADER.toLowerCase(), "adler=:MDNmYzAxOWQ:"));
    Path filePath = directory.resolve("tmpFile");
    Files.createFile(filePath);
    when(eah.getChecksumAttribute(filePath)).thenReturn("03fc019d");
    when(resolver.getPath(FULL_LOCAL_PATH)).thenReturn(filePath);

    miltonBehaviour.putRequestHandling(request);

    assertThat(Files.exists(filePath), is(true));
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

    assertThat(checksumVerificationError.getMessage(), is("client/server checksum mismatch"));
    assertThat(Files.exists(filePath), is(false));
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

    assertThat(
        checksumVerificationError.getMessage(),
        is("Error retrieving checksum from the file system"));
    assertThat(Files.exists(filePath), is(true));
  }
}
