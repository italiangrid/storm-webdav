// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.utils;

import com.google.common.io.CountingOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/** */
public final class StormCountingOutputStream extends FilterOutputStream implements Countable {

  final CountingOutputStream delegate;

  final Path path;

  private StormCountingOutputStream(CountingOutputStream out, Path p) {
    super(out);
    delegate = out;
    this.path = p;
  }

  public long getCount() {
    return delegate.getCount();
  }

  public Path getPath() {
    return path;
  }

  public static StormCountingOutputStream create(OutputStream out, Path path) {
    CountingOutputStream cout = new CountingOutputStream(out);
    return new StormCountingOutputStream(cout, path);
  }
}
