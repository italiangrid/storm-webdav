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
