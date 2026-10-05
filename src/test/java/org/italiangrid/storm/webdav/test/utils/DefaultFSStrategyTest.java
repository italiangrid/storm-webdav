// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.micrometer.observation.ObservationRegistry;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Adler32;
import org.italiangrid.storm.webdav.fs.DefaultFSStrategy;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;

class DefaultFSStrategyTest {

  @TempDir Path testFolder;

  @Autowired ObservationRegistry observationRegistry;

  @Test
  void createCopiesStreamAndSetsAdler32Checksum() throws IOException {
    ExtendedAttributesHelper attrsHelper = mock(ExtendedAttributesHelper.class);
    DefaultFSStrategy strategy = new DefaultFSStrategy(attrsHelper, observationRegistry);

    byte[] payload =
        ("This is a test payload for stream upload checksum validation.\n"
                + "It is intentionally larger than the default copy buffer to exercise "
                + "the transfer path used by the webdav upload API.")
            .getBytes(StandardCharsets.UTF_8);

    File testFile = testFolder.resolve("test.txt").toFile();

    strategy.create(testFile, new ByteArrayInputStream(payload));

    assertArrayEquals(payload, Files.readAllBytes(testFile.toPath()));

    ArgumentCaptor<String> checksumCaptor = ArgumentCaptor.forClass(String.class);
    verify(attrsHelper)
        .setChecksumAttribute(ArgumentMatchers.eq(testFile), checksumCaptor.capture());

    String expected = expectedAdler32Hex(payload);
    assertEquals(expected, checksumCaptor.getValue());
  }

  private static String expectedAdler32Hex(byte[] payload) {
    Adler32 adler32 = new Adler32();
    adler32.update(payload);
    return String.format("%08x", adler32.getValue());
  }
}
