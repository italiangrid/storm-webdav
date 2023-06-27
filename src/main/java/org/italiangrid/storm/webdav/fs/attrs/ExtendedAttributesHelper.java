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

public interface ExtendedAttributesHelper {

  public void setExtendedFileAttribute(File f, String attributeName,
    String attributeValue) throws IOException;

  public String getExtendedFileAttributeValue(File f, String attributeName)
    throws IOException;

  public List<String> getExtendedFileAttributeNames(File f) throws IOException;

  public void setChecksumAttribute(Path p, String checksumValue)
    throws IOException;
  
  public void setChecksumAttribute(File f, String checksumValue)
    throws IOException;

  public String getChecksumAttribute(File f) throws IOException;
  
  public String getChecksumAttribute(Path p) throws IOException;

  public boolean fileSupportsExtendedAttributes(File f) throws IOException;
}
