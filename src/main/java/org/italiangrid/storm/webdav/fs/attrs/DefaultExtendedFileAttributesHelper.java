// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.fs.attrs;

import com.sun.jna.platform.linux.XAttrUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Collection;
import java.util.Objects;
import org.springframework.util.Assert;

public class DefaultExtendedFileAttributesHelper implements ExtendedAttributesHelper {

  private static final String USERDEFINEDFILEATTRIBUTEVIEW_NOT_SUPPORTED_MESSAGE =
      "UserDefinedFileAttributeView not supported on file ";

  // Namespace for extended user attributes
  public static final String USER_NAMESPACE = "user.";

  public static final String STORM_ADLER32_CHECKSUM_ATTR_NAME = "storm.checksum.adler32";

  public static final String STORM_MIGRATED_ATTR_NAME = "storm.migrated";

  public static final String STORM_PREMIGRATED_ATTR_NAME = "storm.premigrated";

  public DefaultExtendedFileAttributesHelper() {}

  protected String getAttributeValue(File f, String attributeName) throws IOException {
    String userAttributeName = USER_NAMESPACE + attributeName;
    String path = f.toPath().toString();
    Collection<String> attrNames = getExtendedFileAttributeNames(f);
    if (attrNames.contains(userAttributeName)) {
      // XAttrUtil is used because UserDefinedFileAttributeView do an openat potentially triggering
      // a transparent recall
      return XAttrUtil.getXAttr(path, userAttributeName);
    }
    return null;
  }

  @Override
  public void setExtendedFileAttribute(File f, String attributeName, String attributeValue)
      throws IOException {

    Objects.requireNonNull(f);
    Assert.hasText(attributeName, "Attribute name must not be empty");

    UserDefinedFileAttributeView faView =
        Files.getFileAttributeView(f.toPath(), UserDefinedFileAttributeView.class);

    if (faView == null) {
      throw new IOException(
          USERDEFINEDFILEATTRIBUTEVIEW_NOT_SUPPORTED_MESSAGE + f.getAbsolutePath());
    }

    faView.write(attributeName, StandardCharsets.UTF_8.encode(attributeValue));
  }

  @Override
  public String getExtendedFileAttributeValue(File f, String attributeName) throws IOException {

    Objects.requireNonNull(f);
    Assert.hasText(attributeName, "Attribute name must not be empty");

    return getAttributeValue(f, attributeName);
  }

  @Override
  public Collection<String> getExtendedFileAttributeNames(File f) throws IOException {

    Objects.requireNonNull(f);
    String path = f.toPath().toString();
    // XAttrUtil is used because UserDefinedFileAttributeView do an openat potentially triggering a
    // transparent recall
    return XAttrUtil.listXAttr(path);
  }

  @Override
  public void setChecksumAttribute(File f, String checksumValue) throws IOException {

    if (fileSupportsExtendedAttributes(f)) {
      setExtendedFileAttribute(f, STORM_ADLER32_CHECKSUM_ATTR_NAME, checksumValue);
    }
  }

  @Override
  public void setPremigratedAttribute(Path p) throws IOException {
    setPremigratedAttribute(p.toFile());
  }

  @Override
  public void setPremigratedAttribute(File f) throws IOException {
    if (fileSupportsExtendedAttributes(f)) {
      setExtendedFileAttribute(f, STORM_PREMIGRATED_ATTR_NAME, "");
    }
  }

  @Override
  public String getChecksumAttribute(File f) throws IOException {

    return getExtendedFileAttributeValue(f, STORM_ADLER32_CHECKSUM_ATTR_NAME);
  }

  @Override
  public String getMigratedAttribute(File f) throws IOException {
    return getExtendedFileAttributeValue(f, STORM_MIGRATED_ATTR_NAME);
  }

  @Override
  public boolean fileSupportsExtendedAttributes(File f) throws IOException {

    Objects.requireNonNull(f);

    UserDefinedFileAttributeView faView =
        Files.getFileAttributeView(f.toPath(), UserDefinedFileAttributeView.class);

    return (faView != null);
  }

  @Override
  public void setChecksumAttribute(Path p, String checksumValue) throws IOException {
    setChecksumAttribute(p.toFile(), checksumValue);
  }

  @Override
  public String getChecksumAttribute(Path p) throws IOException {
    return getChecksumAttribute(p.toFile());
  }

  @Override
  public String getMigratedAttribute(Path p) throws IOException {
    return getMigratedAttribute(p.toFile());
  }
}
