// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
    return ChecksumHelper.addLeadingZero(
        Long.toHexString(getChecksum().getValue()), ADLER32_CHECKSUM_LENGTH);
  }
}
