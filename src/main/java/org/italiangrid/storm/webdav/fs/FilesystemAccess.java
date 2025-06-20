// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FilesystemAccess {

  File mkdir(File parentDirectory, String dirName);

  void rm(File f) throws IOException;

  void mv(File source, File dest);

  void cp(File source, File dest);

  File[] ls(File dir, int limit);

  File create(File file, InputStream in);
}
