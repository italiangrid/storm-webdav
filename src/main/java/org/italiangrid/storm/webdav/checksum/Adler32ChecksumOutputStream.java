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
package org.italiangrid.storm.webdav.checksum;

import static org.italiangrid.storm.webdav.utils.ChecksumHelper.ADLER32_CHECKSUM_LENGTH;

import java.io.OutputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;

import org.italiangrid.storm.webdav.utils.ChecksumHelper;

public class Adler32ChecksumOutputStream extends CheckedOutputStream {

  public Adler32ChecksumOutputStream(OutputStream out) {
    super(out, new Adler32());
  }

  public String getChecksumValue() {
    return ChecksumHelper.addLeadingZero(Long.toHexString(getChecksum().getValue()),
        ADLER32_CHECKSUM_LENGTH);
  }

}
