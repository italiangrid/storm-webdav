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
package org.italiangrid.storm.webdav.test.checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.italiangrid.storm.webdav.checksum.Adler32ChecksumInputStream;
import org.italiangrid.storm.webdav.utils.ChecksumHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChecksumHelperTest {

  private File testFile;
  private Adler32ChecksumInputStream cis;

  @BeforeEach
  public void setup() throws IOException {

    String resourcePath = "storage/test/example";

    ClassLoader classLoader = getClass().getClassLoader();
    testFile = new File(classLoader.getResource(resourcePath).getFile());

    String absolutePath = testFile.getAbsolutePath();
    System.out.println(absolutePath);

    assertTrue(absolutePath.endsWith("example"));

    cis = new Adler32ChecksumInputStream(new BufferedInputStream(new FileInputStream(testFile)));

    byte[] buffer = new byte[8192];

    while (cis.read(buffer) != -1) {
      // do nothing, just read
    }
  }

  @AfterEach
  public void cleanup() throws IOException {
    cis.close();
  }

  @Test
  void testGetChecksumValueFromFile() {

    String newChecksum = cis.getChecksumValue();
    String oldChecksum = Long.toHexString(cis.getChecksum().getValue());
    assertEquals(8, newChecksum.length());
    assertEquals("0bc002ed", newChecksum);
    assertEquals(7, oldChecksum.length());
    assertEquals("bc002ed", oldChecksum);
  }

  @Test
  void testChecksumHelperAddLeadingZero() {

    final String CHECKSUM_VALUE = "abcdefgh";
    assertEquals(CHECKSUM_VALUE, ChecksumHelper.addLeadingZero(CHECKSUM_VALUE, 8));
    assertEquals("0bcdefgh", ChecksumHelper.addLeadingZero(CHECKSUM_VALUE.substring(1), 8));
    assertEquals("00cdefgh", ChecksumHelper.addLeadingZero(CHECKSUM_VALUE.substring(2), 8));
    assertEquals("000defgh", ChecksumHelper.addLeadingZero(CHECKSUM_VALUE.substring(3), 8));
    assertEquals("0000efgh", ChecksumHelper.addLeadingZero(CHECKSUM_VALUE.substring(4), 8));
    assertEquals("00000fgh", ChecksumHelper.addLeadingZero(CHECKSUM_VALUE.substring(5), 8));
    assertEquals("000000gh", ChecksumHelper.addLeadingZero(CHECKSUM_VALUE.substring(6), 8));
    assertEquals("0000000h", ChecksumHelper.addLeadingZero(CHECKSUM_VALUE.substring(7), 8));
  }
}
