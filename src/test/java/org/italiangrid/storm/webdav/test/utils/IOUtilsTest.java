// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.italiangrid.storm.webdav.utils.RangeCopyHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IOUtilsTest {

  @TempDir public File testFolder;

  public File tempFileOfChar(String name, int b, int size) throws IOException {
    File f = new File(testFolder, name);

    try (OutputStream fos = Files.newOutputStream(f.toPath())) {
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

    assertEquals(
        16L, RangeCopyHelper.rangeCopy(Files.newInputStream(source.toPath()), dest, 0, 16));

    try (InputStream fis = Files.newInputStream(dest.toPath())) {
      for (int i = 0; i < 16; i++) {
        assertEquals(0, fis.read(), "Expected 0 but read something else");
      }

      for (int i = 16; i < dest.length(); i++) {
        assertEquals(1, fis.read(), "Expected 1 but read something else");
      }
    }
  }

  @Test
  void testLongerSourceFileCopy() throws IOException {
    File source = tempFileOfChar("source", 0, 200);
    File dest = tempFileOfChar("dest", 1, 64);

    assertEquals(
        16L, RangeCopyHelper.rangeCopy(Files.newInputStream(source.toPath()), dest, 0, 16));

    try (InputStream fis = Files.newInputStream(dest.toPath())) {
      for (int i = 0; i < 16; i++) {
        assertEquals(0, fis.read(), "Expected 0 but read something else");
      }

      for (int i = 16; i < dest.length(); i++) {
        assertEquals(1, fis.read(), "Expected 1 but read something else");
      }
    }
  }

  @Test
  void testShorterSourceFileCopy() throws IOException {
    File source = tempFileOfChar("source", 0, 16);
    File dest = tempFileOfChar("dest", 1, 512);

    assertEquals(
        16L, RangeCopyHelper.rangeCopy(Files.newInputStream(source.toPath()), dest, 0, 16));

    try (InputStream fis = Files.newInputStream(dest.toPath())) {
      for (int i = 0; i < 16; i++) {
        assertEquals(0, fis.read(), "Expected 0 but read something else");
      }

      for (int i = 16; i < dest.length(); i++) {
        assertEquals(1, fis.read(), "Expected 1 but read something else");
      }
    }
  }

  @Test
  void testPartialWriteCopy() throws IOException {
    File source = tempFileOfChar("source", 0, 128);
    File dest = tempFileOfChar("dest", 1, 512);

    assertEquals(
        12L, RangeCopyHelper.rangeCopy(Files.newInputStream(source.toPath()), dest, 500, 12));

    try (InputStream fis = Files.newInputStream(dest.toPath())) {
      for (int i = 0; i < 500; i++) {
        assertEquals(1, fis.read(), "Expected 1 but read something else");
      }

      for (int i = 500; i < dest.length(); i++) {
        assertEquals(0, fis.read(), "Expected 0 but read something else");
      }
    }
  }

  @Test
  void testMiddleWrite() throws IOException {
    File source = tempFileOfChar("source", 0, 640);
    File dest = tempFileOfChar("dest", 1, 256);

    try (InputStream fisSrc = Files.newInputStream(source.toPath())) {
      assertEquals(100L, RangeCopyHelper.rangeCopy(fisSrc, dest, 100, 100));
    }

    try (InputStream fis = Files.newInputStream(dest.toPath())) {
      for (int i = 0; i < 100; i++) {
        assertEquals(1, fis.read(), "Expected 1");
      }

      for (int i = 100; i < 200; i++) {
        assertEquals(0, fis.read(), "Expected 0");
      }

      for (int i = 200; i < dest.length(); i++) {
        assertEquals(1, fis.read(), "Expected 1");
      }
    }
  }
}
