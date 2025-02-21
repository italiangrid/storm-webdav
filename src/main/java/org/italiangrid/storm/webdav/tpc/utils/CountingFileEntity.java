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
package org.italiangrid.storm.webdav.tpc.utils;

import static org.apache.hc.core5.http.ContentType.APPLICATION_OCTET_STREAM;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hc.core5.http.io.entity.FileEntity;

import com.google.common.io.CountingOutputStream;

public class CountingFileEntity extends FileEntity implements Countable {

  private CountingOutputStream os;

  private CountingFileEntity(File file) {
    super(file, APPLICATION_OCTET_STREAM);
  }

  @Override
  public long getCount() {
    if (os == null) {
      return 0;
    }

    return os.getCount();
  }


  @Override
  public void writeTo(OutputStream outstream) throws IOException {
    os = new CountingOutputStream(outstream);
    super.writeTo(os);
  }

  public static CountingFileEntity create(File f) {
    return new CountingFileEntity(f);
  }

}
