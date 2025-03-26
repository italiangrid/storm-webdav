// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server;

import java.nio.file.Path;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.fs.Locality;

public interface PathResolver {

  String resolvePath(String pathInContext);

  Path getPath(String pathInContext);

  StorageAreaInfo resolveStorageArea(String pathInContext);

  boolean pathExists(String pathInContext);

  boolean isStub(String pathInContext);

  Locality getLocality(String pathInContext);
}
