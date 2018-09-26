package org.italiangrid.storm.webdav.tpc.utils;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.CountingOutputStream;

/**
 * 
 *
 */
public class StormCountingOutputStream extends FilterOutputStream {

  final CountingOutputStream delegate;
  
  final Path path;

  private StormCountingOutputStream(CountingOutputStream out, String p) {
    super(out);
    delegate = out;
    this.path = Paths.get(p);
  }

  public long getCount() {
    return delegate.getCount();
  }
  
  public Path getPath() {
    return path;
  }

  public static StormCountingOutputStream create(OutputStream out, String path) {
    CountingOutputStream cout = new CountingOutputStream(out);
    return new StormCountingOutputStream(cout, path);
  }
}
