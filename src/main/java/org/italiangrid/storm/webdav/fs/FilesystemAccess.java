// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FilesystemAccess {

  public File mkdir(File parentDirectory, String dirName);

  public void rm(File f) throws IOException;

  public void mv(File source, File dest);

  public void cp(File source, File dest);

  public File[] ls(File dir, int limit);

  public File create(File file, InputStream in);
}
