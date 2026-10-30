// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.milton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import org.italiangrid.storm.webdav.fs.DefaultFSStrategy;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.milton.StoRMDirectoryResource;
import org.italiangrid.storm.webdav.milton.StoRMResource;
import org.italiangrid.storm.webdav.milton.StoRMResourceFactory;
import org.italiangrid.storm.webdav.milton.util.ReplaceContentStrategy;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;

class StoRMResourceTests {

  @TempDir Path directory;
  private static final String NAME = "file";
  private static final String DEST_NAME = "destFile";
  private Path path;
  private Path destination;

  @Mock Request request;
  @Mock Method method;
  @Mock Auth auth;
  @Mock ExtendedAttributesHelper helper;
  @Mock ReplaceContentStrategy strategy;
  @Mock PathResolver resolver;

  private final FilesystemAccess access = new DefaultFSStrategy(helper, null);
  private final StoRMResourceFactory factory =
      new StoRMResourceFactory(access, helper, resolver, strategy);
  private File file;
  private File destinationFile;
  private StoRMResource resource;

  @BeforeEach
  void setup() {
    path = directory.resolve(NAME);
    destination = directory.resolve(DEST_NAME);

    file = new File(path.toString());
    destinationFile = new File(destination.toString());
    resource = new StoRMDirectoryResource(factory, file);
  }

  @Test
  void getNameTest() {
    assertEquals(NAME, resource.getName());
  }

  @Test
  void getUniqueIdTest() {
    assertEquals(path.toString(), resource.getUniqueId());
  }

  @Test
  void getCreateDateTest() {
    assertEquals(new Date(file.lastModified()), resource.getCreateDate());
  }

  @Test
  void getRealmTest() {
    assertEquals(null, resource.getRealm());
  }

  @Test
  void authenticateTest() {
    final String user = "user";
    assertEquals(user, resource.authenticate(user, "password"));
  }

  @Test
  void authoriseTest() {
    assertTrue(resource.authorise(request, method, auth));
  }

  @Test
  void checkRedirectTest() throws NotAuthorizedException, BadRequestException {
    assertEquals(null, resource.checkRedirect(request));
  }

  @Test
  void getFileTest() {
    assertEquals(file, resource.getFile());
  }

  @Test
  void getResourceFactory() {
    assertEquals(factory, resource.getResourceFactory());
  }

  @Test
  void getFilesystemAccessTest() {
    assertEquals(access, resource.getFilesystemAccess());
  }

  @Test
  void getExtendedAttributesHelperTest() throws IOException {
    Files.createFile(file.toPath());
    assertEquals(helper, resource.getExtendedAttributesHelper());
  }

  @Test
  void moveToTest()
      throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
    final StoRMDirectoryResource rDest = new StoRMDirectoryResource(factory, directory.toFile());
    Files.createFile(file.toPath());

    assertTrue(file.isFile());
    assertFalse(destinationFile.isFile());

    resource.moveTo(rDest, DEST_NAME);

    assertFalse(file.isFile());
    assertTrue(destinationFile.isFile());
  }
}
