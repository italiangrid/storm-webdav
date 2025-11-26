// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.checksum;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.milton.http.Request;
import io.milton.http.Response;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.milton.util.ReplaceContentStrategy;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.server.servlet.MiltonFilter;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PutRequestChecksumTest {

  public static final String SERVLET_PATH = "/test";
  public static final String LOCAL_PATH = "/some/file";

  public static final String FULL_LOCAL_PATH = SERVLET_PATH + LOCAL_PATH;

  @TempDir Path directory;

  @Mock HttpServletRequest request;

  @Mock Request miltonReq;

  @Mock Response miltonRes;

  @Mock StorageAreaInfo storageAreaInfo;

  @Mock FilesystemAccess fsAccess;

  @Mock ExtendedAttributesHelper eah;

  @Mock PathResolver resolver;

  @Mock ReplaceContentStrategy rcs;

  MiltonFilter filter;

  @Captor ArgumentCaptor<String> error;

  @Captor ArgumentCaptor<Response.Status> httpStatus;

  @BeforeEach
  void setup() {
    filter = new MiltonFilter(fsAccess, eah, resolver, rcs);
    lenient().when(miltonReq.getAbsolutePath()).thenReturn(FULL_LOCAL_PATH);
  }

  @Test
  void reprDigestHeaderNotSended() throws IOException {
    Path filePath = directory.resolve("tmpFile");
    Files.createFile(filePath);

    filter.putRequestHandling(request, miltonReq, miltonRes, storageAreaInfo);

    verify(resolver, never()).getPath(miltonReq.getAbsolutePath());
    verify(miltonRes, never()).sendError(httpStatus.capture(), error.capture());
    assertThat(Files.exists(filePath), is(true));
  }

  @Test
  void checksumsMatchSoFileIsNotDeleted() throws IOException {
    when(request.getHeader(TransferConstants.REPR_DIGEST_HEADER)).thenReturn("adler=:MDNmYzAxOWQ:");
    Path filePath = directory.resolve("tmpFile");
    Files.createFile(filePath);
    when(eah.getChecksumAttribute(filePath)).thenReturn("03fc019d");
    when(resolver.getPath(FULL_LOCAL_PATH)).thenReturn(filePath);

    filter.putRequestHandling(request, miltonReq, miltonRes, storageAreaInfo);

    verify(miltonRes, never()).sendError(httpStatus.capture(), error.capture());
    assertThat(Files.exists(filePath), is(true));
  }

  @Test
  void checksumsMismatchSoFileIsDeleted() throws IOException {
    when(request.getHeader(TransferConstants.REPR_DIGEST_HEADER)).thenReturn("adler=:MDNmYzAxOWQ:");
    Path filePath = directory.resolve("tmpFile");
    Files.createFile(filePath);
    when(eah.getChecksumAttribute(filePath)).thenReturn("045d01c1");
    when(resolver.getPath(FULL_LOCAL_PATH)).thenReturn(filePath);

    filter.putRequestHandling(request, miltonReq, miltonRes, storageAreaInfo);

    verify(miltonRes).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(Response.Status.SC_PRECONDITION_FAILED));
    assertThat(error.getValue(), is("client/server checksum mismatch"));
    assertThat(Files.exists(filePath), is(false));
  }

  @Test
  void checksumNotAvailableFailsButFileNotDeleted() throws IOException {
    when(request.getHeader(TransferConstants.REPR_DIGEST_HEADER)).thenReturn("adler=:MDNmYzAxOWQ:");
    Path filePath = directory.resolve("tmpFile");
    Files.createFile(filePath);
    Mockito.doThrow(new IOException(""))
        .when(eah)
        .getChecksumAttribute(ArgumentMatchers.<Path>any());
    when(resolver.getPath(FULL_LOCAL_PATH)).thenReturn(filePath);

    filter.putRequestHandling(request, miltonReq, miltonRes, storageAreaInfo);

    verify(miltonRes).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(Response.Status.SC_PRECONDITION_FAILED));
    assertThat(error.getValue(), is("Error retrieving checksum from the file system"));
    assertThat(Files.exists(filePath), is(true));
  }
}
