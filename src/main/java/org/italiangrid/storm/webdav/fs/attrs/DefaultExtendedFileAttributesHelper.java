// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.fs.attrs;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.List;
import java.util.Objects;
import org.springframework.util.Assert;

import io.opentelemetry.instrumentation.annotations.WithSpan;

public class DefaultExtendedFileAttributesHelper implements ExtendedAttributesHelper {

  private static final String USERDEFINEDFILEATTRIBUTEVIEW_NOT_SUPPORTED_MESSAGE =
      "UserDefinedFileAttributeView not supported on file ";

  public static final String STORM_ADLER32_CHECKSUM_ATTR_NAME = "storm.checksum.adler32";

  public DefaultExtendedFileAttributesHelper() {}

  @WithSpan
  protected String getAttributeValue(UserDefinedFileAttributeView view, String attributeName)
      throws IOException {

    if (view.list().contains(attributeName)) {
      ByteBuffer buffer = ByteBuffer.allocateDirect(view.size(attributeName));
      view.read(attributeName, buffer);
      buffer.flip();
      return StandardCharsets.UTF_8.decode(buffer).toString();
    } else {
      return "";
    }
  }

  @WithSpan
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

  @WithSpan
  @Override
  public String getExtendedFileAttributeValue(File f, String attributeName) throws IOException {

    Objects.requireNonNull(f);
    Assert.hasText(attributeName, "Attribute name must not be empty");

    UserDefinedFileAttributeView faView =
        Files.getFileAttributeView(f.toPath(), UserDefinedFileAttributeView.class);

    if (faView == null) {
      throw new IOException(
          USERDEFINEDFILEATTRIBUTEVIEW_NOT_SUPPORTED_MESSAGE + f.getAbsolutePath());
    }

    return getAttributeValue(faView, attributeName);
  }

  @WithSpan
  @Override
  public List<String> getExtendedFileAttributeNames(File f) throws IOException {

    Objects.requireNonNull(f);

    UserDefinedFileAttributeView faView =
        Files.getFileAttributeView(f.toPath(), UserDefinedFileAttributeView.class);

    if (faView == null) {
      throw new IOException(
          USERDEFINEDFILEATTRIBUTEVIEW_NOT_SUPPORTED_MESSAGE + f.getAbsolutePath());
    }

    return faView.list();
  }

  @WithSpan
  @Override
  public void setChecksumAttribute(File f, String checksumValue) throws IOException {

    if (fileSupportsExtendedAttributes(f)) {
      setExtendedFileAttribute(f, STORM_ADLER32_CHECKSUM_ATTR_NAME, checksumValue);
    }
  }

  @WithSpan
  @Override
  public String getChecksumAttribute(File f) throws IOException {

    return getExtendedFileAttributeValue(f, STORM_ADLER32_CHECKSUM_ATTR_NAME);
  }

  @WithSpan
  @Override
  public boolean fileSupportsExtendedAttributes(File f) throws IOException {

    Objects.requireNonNull(f);

    UserDefinedFileAttributeView faView =
        Files.getFileAttributeView(f.toPath(), UserDefinedFileAttributeView.class);

    return (faView != null);
  }

  @WithSpan
  @Override
  public void setChecksumAttribute(Path p, String checksumValue) throws IOException {
    setChecksumAttribute(p.toFile(), checksumValue);
  }

  @WithSpan
  @Override
  public String getChecksumAttribute(Path p) throws IOException {
    return getChecksumAttribute(p.toFile());
  }
}
