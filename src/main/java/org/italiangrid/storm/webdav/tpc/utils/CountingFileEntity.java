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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.InputStreamEntity;

import com.google.common.io.CountingOutputStream;

public class CountingFileEntity extends InputStreamEntity implements Countable {

  private CountingOutputStream os;

  private CountingFileEntity(InputStream is, long length) {
    super(is, length);
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
  
  public static CountingFileEntity create(File f) throws FileNotFoundException {
    FileInputStream fis = new FileInputStream(f);
    return new CountingFileEntity(fis,f.length());
  }
  
}
