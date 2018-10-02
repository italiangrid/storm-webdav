/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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
public class StormCountingOutputStream extends FilterOutputStream implements Countable{

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
