package org.italiangrid.storm.webdav.fs;

import java.io.File;
import java.io.InputStream;

public interface FilesystemAccess {

  public File mkdir(File parentDirectory, String dirName);

  public boolean rm(File f);

  public void mv(File source, File dest);

  public void cp(File source, File dest);

  public File[] ls(File dir, int limit);

  public File create(File file, InputStream in);

}
