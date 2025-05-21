// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.fs.attrs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface ExtendedAttributesHelper {

  public void setExtendedFileAttribute(File f, String attributeName, String attributeValue)
      throws IOException;

  public String getExtendedFileAttributeValue(File f, String attributeName) throws IOException;

  public Collection<String> getExtendedFileAttributeNames(File f) throws IOException;

  public void setChecksumAttribute(Path p, String checksumValue) throws IOException;

  public void setChecksumAttribute(File f, String checksumValue) throws IOException;

  public void setPremigrateAttribute(Path p) throws IOException;

  public void setPremigrateAttribute(File f) throws IOException;

  public String getChecksumAttribute(File f) throws IOException;

  public String getChecksumAttribute(Path p) throws IOException;

  public String getMigratedAttribute(File f) throws IOException;

  public String getMigratedAttribute(Path p) throws IOException;

  public boolean fileSupportsExtendedAttributes(File f) throws IOException;
}
