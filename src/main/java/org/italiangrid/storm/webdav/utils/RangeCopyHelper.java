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

  public static long rangeCopy(InputStream is, File f, long rangeStart, long rangeCount) throws IOException {

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
