// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
