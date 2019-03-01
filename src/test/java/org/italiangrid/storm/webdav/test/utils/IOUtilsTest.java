/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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
package org.italiangrid.storm.webdav.test.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.italiangrid.storm.webdav.utils.RangeCopyHelper.rangeCopy;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IOUtilsTest {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  public File tempFileOfChar(String name, int b, int size) throws IOException {
    File f = testFolder.newFile(name);

    try (FileOutputStream fos = new FileOutputStream(f)) {
      for (int i = 0; i < size; i++) {
        fos.write(b);
      }
    }

    return f;
  }

  @Test
  public void testAllFileCopy() throws IOException {
    File source = tempFileOfChar("source", 0, 128);
    File dest = tempFileOfChar("dest", 1, 128);

    assertThat(rangeCopy(new FileInputStream(source), dest, 0, 16), is(16L));

    try (FileInputStream fis = new FileInputStream(dest)) {
      for (int i = 0; i < 16; i++) {
        assertThat("Expected 0 but read something else", fis.read(), is(0));
      }

      for (int i = 16; i < dest.length(); i++) {
        assertThat("Expected 1 but read something else", fis.read(), is(1));
      }
    }
  }

  @Test
  public void testLongerSourceFileCopy() throws IOException {
    File source = tempFileOfChar("source", 0, 200);
    File dest = tempFileOfChar("dest", 1, 64);

    assertThat(rangeCopy(new FileInputStream(source), dest, 0, 16), is(16L));

    try (FileInputStream fis = new FileInputStream(dest)) {
      for (int i = 0; i < 16; i++) {
        assertThat("Expected 0 but read something else", fis.read(), is(0));
      }

      for (int i = 16; i < dest.length(); i++) {
        assertThat("Expected 1 but read something else", fis.read(), is(1));
      }
    }
  }

  @Test
  public void testShorterSourceFileCopy() throws IOException {
    File source = tempFileOfChar("source", 0, 16);
    File dest = tempFileOfChar("dest", 1, 512);

    assertThat(rangeCopy(new FileInputStream(source), dest, 0, 16), is(16L));

    try (FileInputStream fis = new FileInputStream(dest)) {
      for (int i = 0; i < 16; i++) {
        assertThat("Expected 0 but read something else", fis.read(), is(0));
      }

      for (int i = 16; i < dest.length(); i++) {
        assertThat("Expected 1 but read something else", fis.read(), is(1));
      }
    }
  }

  @Test
  public void testPartialWriteCopy() throws IOException {
    File source = tempFileOfChar("source", 0, 128);
    File dest = tempFileOfChar("dest", 1, 512);

    assertThat(rangeCopy(new FileInputStream(source), dest, 500, 12), is(12L));

    try (FileInputStream fis = new FileInputStream(dest)) {
      for (int i = 0; i < 500; i++) {
        assertThat("Expected 1 but read something else", fis.read(), is(1));
      }

      for (int i = 500; i < dest.length(); i++) {
        assertThat("Expected 0 but read something else", fis.read(), is(0));
      }
    }
  }

  @Test
  public void testMiddleWrite() throws IOException {
    File source = tempFileOfChar("source", 0, 640);
    File dest = tempFileOfChar("dest", 1, 256);

    try (FileInputStream fisSrc = new FileInputStream(source)) {
      assertThat(rangeCopy(fisSrc, dest, 100, 100), is(100L));
    }

    try (FileInputStream fis = new FileInputStream(dest)) {
      for (int i = 0; i < 100; i++) {
        assertThat("Expected 1", fis.read(), is(1));
      }

      for (int i = 100; i < 200; i++) {
        assertThat("Expected 0", fis.read(), is(0));
      }
      
      for (int i = 200; i < dest.length(); i++) {
        assertThat("Expected 1", fis.read(), is(1));
      }
    }
  }

}
