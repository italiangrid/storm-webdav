// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.milton;

import static io.milton.property.PropertySource.PropertyAccessibility.READ_ONLY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.micrometer.observation.ObservationRegistry;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.property.PropertySource.PropertyMetaData;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.italiangrid.storm.webdav.fs.DefaultFSStrategy;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.DefaultExtendedFileAttributesHelper;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.milton.StoRMDirectoryResource;
import org.italiangrid.storm.webdav.milton.StoRMFileResource;
import org.italiangrid.storm.webdav.milton.StoRMResourceFactory;
import org.italiangrid.storm.webdav.milton.util.EarlyChecksumStrategy;
import org.italiangrid.storm.webdav.milton.util.ReplaceContentStrategy;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;

class StoRMFileResourceTests {

  @TempDir Path directory;
  private static final String NAME = "file";
  private static final String DEST_NAME = "destFile";
  private static final String NAMESPACE_URI = "http://storm.italiangrid.org/2014/webdav";
  private static final String CHECKSUM = "Checksum";
  private Path path;
  private Path destination;
  @Mock ObservationRegistry registry;
  private ExtendedAttributesHelper helper = new DefaultExtendedFileAttributesHelper(registry);

  @Mock Request request;
  @Mock Method method;
  @Mock Auth auth;
  ReplaceContentStrategy strategy = new EarlyChecksumStrategy(helper);
  @Mock PathResolver resolver;

  private final FilesystemAccess access = new DefaultFSStrategy(helper, null);
  private final StoRMResourceFactory factory =
      new StoRMResourceFactory(access, helper, resolver, strategy);
  private File file;
  private File destinationFile;
  private StoRMFileResource resource;

  private static final QName METADATA_NAME = new QName(NAMESPACE_URI, CHECKSUM);

  private static final Map<QName, PropertyMetaData> METADATA =
      Map.of(METADATA_NAME, new PropertyMetaData(READ_ONLY, String.class));

  @BeforeEach
  void setup() throws IOException {
    path = directory.resolve(NAME);
    destination = directory.resolve(DEST_NAME);

    file = new File(path.toString());
    destinationFile = new File(destination.toString());
    resource = new StoRMFileResource(factory, file);
    Files.writeString(path, "Old file content");
  }

  @Test
  void deleteTest() throws ConflictException, NotAuthorizedException, BadRequestException {
    assertTrue(file.isFile());

    resource.delete();
    assertFalse(file.isFile());
  }

  @Test
  void copyToTest() throws ConflictException, NotAuthorizedException, BadRequestException {
    final StoRMDirectoryResource rDest = new StoRMDirectoryResource(factory, directory.toFile());

    assertTrue(file.isFile());
    assertFalse(destinationFile.isFile());

    resource.copyTo(rDest, DEST_NAME);

    assertTrue(file.isFile());
    assertTrue(destinationFile.isFile());
  }

  @Test
  void toStringTest() {
    assertEquals(
        "StoRMFileResource [resourceFactory=" + factory + ", file=" + file + "]",
        resource.toString());
  }

  @Test
  void getContentLengthTest() {
    assertEquals(file.length(), resource.getContentLength());
  }

  @Test
  void getContentTypeTest() {
    final FileNameMap typeMap = URLConnection.getFileNameMap();
    assertEquals(
        typeMap.getContentTypeFor(resource.getFile().getAbsolutePath()),
        resource.getContentType(null));
  }

  @Test
  void getMaxAgeSecondsTest() {
    Auth authMock = mock(Auth.class);
    assertEquals(null, resource.getMaxAgeSeconds(null));
    assertEquals(null, resource.getMaxAgeSeconds(authMock));
  }

  @Test
  void sendContentTest() {
    OutputStream out = mock(OutputStream.class);
    Range range = mock(Range.class);
    Map<String, String> params = new HashMap<>();

    Exception e =
        assertThrows(
            UnsupportedOperationException.class,
            () -> {
              resource.sendContent(out, range, params, "application/json");
            });
    assertTrue(e instanceof UnsupportedOperationException);
  }

  @Test
  void getAllPropertyNamesTest() {
    assertEquals(
        new ArrayList<>(METADATA.keySet()).toString(), resource.getAllPropertyNames().toString());
  }

  @Test
  void getPropertyMetaDataTest() {
    PropertyMetaData expected = METADATA.get(METADATA_NAME);
    PropertyMetaData result = resource.getPropertyMetaData(METADATA_NAME);
    assertEquals(expected.getAccessibility(), result.getAccessibility());
    assertEquals(expected.getValueType(), result.getValueType());
  }

  @Test
  void setPropertyTest() {
    UnsupportedOperationException e =
        assertThrows(
            UnsupportedOperationException.class,
            () -> {
              resource.setProperty(METADATA_NAME, null);
            });
    assertEquals("StoRM WebDAV does not support setting DAV properties.", e.getMessage());
  }

  private void testPropertyException(QName wrongName) {
    StoRMWebDAVError e =
        assertThrows(StoRMWebDAVError.class, () -> resource.getProperty(wrongName));
    assertEquals("Unknown property: " + wrongName, e.getMessage());
  }

  @Test
  void getPropertyTest() throws IOException {
    assertEquals(helper.getChecksumAttribute(file), resource.getProperty(METADATA_NAME));
    final String WRONG_URI = "http://generic.domain/something";
    QName wrongName = new QName(WRONG_URI, CHECKSUM);
    testPropertyException(wrongName);

    wrongName = new QName(NAMESPACE_URI, "Different-" + CHECKSUM);
    testPropertyException(wrongName);

    wrongName = new QName(WRONG_URI, "Different-" + CHECKSUM);
    testPropertyException(wrongName);
  }

  private void validateRangeHelper(Range range, InputStream ins, String msg) {
    StoRMWebDAVError err =
        assertThrows(
            StoRMWebDAVError.class,
            () -> {
              resource.replacePartialContent(range, ins);
            });
    assertEquals(msg, err.getMessage());
  }

  @Test
  void validateRangeTest() {
    InputStream ins = mock(InputStream.class);
    validateRangeHelper(new Range(null, null), ins, "Invalid range: range start not defined");
    validateRangeHelper(new Range(100, 200), ins, "Invalid range: range start out of bounds");
    validateRangeHelper(new Range(0, 200), ins, "Invalid range: range end out of bounds");
  }

  @Test
  void replaceContentTest()
      throws IOException, BadRequestException, ConflictException, NotAuthorizedException {
    String partialReplacement = "New";
    InputStream ins = new ByteArrayInputStream(partialReplacement.getBytes(StandardCharsets.UTF_8));
    resource.replacePartialContent(new Range(0, 3), ins);
    assertEquals("New file content", Files.readString(path));

    String replacement = "Very new";
    ins = new ByteArrayInputStream(replacement.getBytes(StandardCharsets.UTF_8));
    resource.replaceContent(ins, Long.valueOf(8));
    assertEquals("Very new content", Files.readString(path));
  }
}
