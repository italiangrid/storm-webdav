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
package org.italiangrid.storm.webdav.test.tape;

import static org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributes.STORM_MIGRATED_ATTR_NAME;
import static org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributes.STORM_RECALL_IN_PROGRESS_ATTR_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.italiangrid.storm.webdav.fs.attrs.DefaultExtendedFileAttributesHelper;
import org.italiangrid.storm.webdav.fs.attrs.FileStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FileStubTest {

  private final static String RESOURCE_BASE_PATH = "storage/tape";

  private File stubFile;
  private File undefinedFile;
  private File onlineFile;
  private File onlineAndMigratedFile;
  private File recallInProgressFile;
  private DefaultExtendedFileAttributesHelper helper = new DefaultExtendedFileAttributesHelper();

  private void printProcess(Process p) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String str = br.readLine();
    while (str != null) {
      System.out.println(str);
      str = br.readLine();
    }
  }

  private File createUndefinedFile(String fileName) throws IOException {

    ClassLoader classLoader = getClass().getClassLoader();
    File resourceBaseDir = new File(classLoader.getResource(RESOURCE_BASE_PATH).getFile());
    File undefinedFile = new File(resourceBaseDir + "/" + fileName);
    printProcess(Runtime.getRuntime()
      .exec("dd if=/dev/zero conv=sparse bs=1M count=1 of=" + undefinedFile.getAbsolutePath()));
    return undefinedFile;
  }

  private File createStubFile(String fileName) throws IOException {

    File stubFile = createUndefinedFile(fileName);
    helper.setExtendedFileAttribute(stubFile, STORM_MIGRATED_ATTR_NAME, "y");
    return stubFile;
  }

  private File createStubRecalledFile(String fileName) throws IOException {

    File stubRecalledFile = createStubFile(fileName);
    helper.setExtendedFileAttribute(stubRecalledFile, STORM_RECALL_IN_PROGRESS_ATTR_NAME, "y");
    return stubRecalledFile;
  }

  private File createOnlineFile(String fileName) throws IOException {

    ClassLoader classLoader = getClass().getClassLoader();
    File resourceBaseDir = new File(classLoader.getResource(RESOURCE_BASE_PATH).getFile());
    File onlineFile = new File(resourceBaseDir + "/" + fileName);
    return onlineFile;
  }

  private File createOnlineAndMigratedFile(String fileName) throws IOException {

    File migratedFile = createOnlineFile(fileName);
    helper.setExtendedFileAttribute(migratedFile, STORM_MIGRATED_ATTR_NAME, "y");
    return migratedFile;
  }

  @BeforeEach
  public void setup() throws IOException {

    undefinedFile = createUndefinedFile("undefined.dat");
    stubFile = createStubFile("tape.dat");
    recallInProgressFile = createStubRecalledFile("tape-recalled.dat");
    onlineFile = createOnlineFile("disk.dat");
    onlineAndMigratedFile = createOnlineAndMigratedFile("disk-and-tape.dat");
  }

  @AfterEach
  public void finalize() throws IOException {

  }

  @Test
  public void testUndefinedFile() throws IOException {
    assertEquals(FileStatus.UNDEFINED, helper.getFileStatus(undefinedFile));
  }

  @Test
  public void testStubFile() throws IOException {
    assertEquals(FileStatus.TAPE, helper.getFileStatus(stubFile));
  }

  @Test
  public void testOnlineFile() throws IOException {
    assertEquals(FileStatus.DISK, helper.getFileStatus(onlineFile));
  }

  @Test
  public void testOnlineAndMigratedFile() throws IOException {
    assertEquals(FileStatus.DISK_AND_TAPE, helper.getFileStatus(onlineAndMigratedFile));
  }

  @Test
  public void testRecallInProgressFile() throws IOException {
    assertEquals(FileStatus.TAPE_RECALL_IN_PROGRESS, helper.getFileStatus(recallInProgressFile));
  }
}
