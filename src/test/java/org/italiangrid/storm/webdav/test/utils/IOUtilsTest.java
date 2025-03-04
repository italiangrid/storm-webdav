// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.utils.RangeCopyHelper.rangeCopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
class IOUtilsTest {

  @TempDir
  public File testFolder;

  public File tempFileOfChar(String name, int b, int size) throws IOException {
    File f = new File(testFolder, name);

    try (FileOutputStream fos = new FileOutputStream(f)) {
      for (int i = 0; i < size; i++) {
        fos.write(b);
      }
    }

    return f;
  }

  @Test
  void testAllFileCopy() throws IOException {
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
  void testLongerSourceFileCopy() throws IOException {
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
  void testShorterSourceFileCopy() throws IOException {
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
  void testPartialWriteCopy() throws IOException {
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
  void testMiddleWrite() throws IOException {
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
