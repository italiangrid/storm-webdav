// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.fs.attrs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface ExtendedAttributesHelper {

  void setExtendedFileAttribute(File f, String attributeName, String attributeValue)
      throws IOException;

  String getExtendedFileAttributeValue(File f, String attributeName) throws IOException;

  Collection<String> getExtendedFileAttributeNames(File f) throws IOException;

  void setChecksumAttribute(Path p, String checksumValue) throws IOException;

  void setChecksumAttribute(File f, String checksumValue) throws IOException;

  void setPremigrateAttribute(Path p) throws IOException;

  void setPremigrateAttribute(File f) throws IOException;

  String getChecksumAttribute(File f) throws IOException;

  String getChecksumAttribute(Path p) throws IOException;

  String getMigratedAttribute(File f) throws IOException;

  String getMigratedAttribute(Path p) throws IOException;

  boolean fileSupportsExtendedAttributes(File f) throws IOException;
}
