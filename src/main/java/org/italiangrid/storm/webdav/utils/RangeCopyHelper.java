// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;
import org.springframework.util.Assert;

public class RangeCopyHelper {

  private RangeCopyHelper() {}

  public static long rangeCopy(InputStream is, File f, long rangeStart, long rangeCount)
      throws IOException {

    Objects.requireNonNull(is);
    Objects.requireNonNull(f);
    Assert.isTrue(rangeStart >= 0, "rangeStart must be >= 0");
    Assert.isTrue(rangeCount > 0, "rangeCount must be > 0");

    try (ReadableByteChannel src = Channels.newChannel(is);
        RandomAccessFile raf = new RandomAccessFile(f, "rw")) {

      FileChannel fc = raf.getChannel();

      long bytesTransferred = 0;

      while (bytesTransferred < rangeCount) {
        long start = rangeStart + bytesTransferred;
        long count = rangeCount - bytesTransferred;
        bytesTransferred = fc.transferFrom(src, start, count);
      }

      return bytesTransferred;
    }
  }
}
