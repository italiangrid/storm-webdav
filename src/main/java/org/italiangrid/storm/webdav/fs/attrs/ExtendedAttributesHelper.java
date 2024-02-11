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
package org.italiangrid.storm.webdav.fs.attrs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import jnr.posix.FileStat;

public interface ExtendedAttributesHelper {

  public boolean fileSupportsExtendedAttributes(File f) throws IOException;

  public List<String> getExtendedFileAttributeNames(File f) throws IOException;

  public void setExtendedFileAttribute(Path p, ExtendedAttributes name, String value) throws IOException;

  public void setExtendedFileAttribute(File f, ExtendedAttributes name, String value) throws IOException;

  public String getExtendedFileAttributeValue(File f, ExtendedAttributes name) throws IOException;

  public FileStat stat(Path p) throws IOException;

  public FileStat stat(File f) throws IOException;

  public FileStatus getFileStatus(Path p) throws IOException;

  public FileStatus getFileStatus(File f) throws IOException;

  public boolean isStub(Path p) throws IOException;

  public boolean isStub(File f) throws IOException;

  public boolean isStub(FileStat fs) throws IOException;
}
