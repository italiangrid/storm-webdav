// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.checksum;

import java.nio.file.Path;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import org.italiangrid.storm.webdav.tpc.utils.Countable;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;
import org.italiangrid.storm.webdav.utils.ChecksumHelper;

public class Adler32ChecksumOutputStream extends CheckedOutputStream implements Countable {

  final StormCountingOutputStream delegate;

  public Adler32ChecksumOutputStream(StormCountingOutputStream out) {
    super(out, new Adler32());
    delegate = out;
  }

  public String getChecksumValue() {
    return ChecksumHelper.addLeadingZero(
        Long.toHexString(getChecksum().getValue()), ChecksumHelper.ADLER32_CHECKSUM_LENGTH);
  }

  public long getCount() {
    return delegate.getCount();
  }

  public Path getPath() {
    return delegate.getPath();
  }
}
