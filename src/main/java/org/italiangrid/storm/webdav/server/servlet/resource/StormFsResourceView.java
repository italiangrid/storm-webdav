// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet.resource;

import java.util.Date;
import org.italiangrid.storm.webdav.fs.Locality;

public record StormFsResourceView(
    String name,
    boolean isDirectory,
    String path,
    long sizeInBytes,
    Date lastModificationTime,
    Locality locality) {
  public StormFsResourceView {
    if (isDirectory && !name.endsWith("/")) {
      name = name + "/";
    }
  }
}
