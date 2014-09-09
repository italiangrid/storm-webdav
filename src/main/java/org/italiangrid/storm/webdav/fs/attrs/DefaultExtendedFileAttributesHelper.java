package org.italiangrid.storm.webdav.fs.attrs;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.List;

import org.springframework.util.Assert;

public class DefaultExtendedFileAttributesHelper implements
  ExtendedAttributesHelper {

  public static final String STORM_ADLER32_CHECKSUM_ATTR_NAME = "storm.checksum.adler32";

  public DefaultExtendedFileAttributesHelper() {

  }

  protected String getAttributeValue(UserDefinedFileAttributeView view,
    String attributeName) throws IOException {

    if (view.list().contains(attributeName)) {
      ByteBuffer buffer = ByteBuffer.allocateDirect(view.size(attributeName));
      view.read(attributeName, buffer);
      buffer.flip();
      return StandardCharsets.UTF_8.decode(buffer).toString();
    } else {
      return "";
    }
  }

  @Override
  public void setExtendedFileAttribute(File f, String attributeName,
    String attributeValue) throws IOException {

    Assert.notNull(f);
    Assert.hasText(attributeName);

    UserDefinedFileAttributeView faView = Files.getFileAttributeView(
      f.toPath(), UserDefinedFileAttributeView.class);

    if (faView == null) {
      throw new IOException(
        "UserDefinedFileAttributeView not supported on file "
          + f.getAbsolutePath());
    }

    faView.write(attributeName, StandardCharsets.UTF_8.encode(attributeValue));
  }

  @Override
  public String getExtendedFileAttributeValue(File f, String attributeName)
    throws IOException {

    Assert.notNull(f);
    Assert.hasText(attributeName);

    UserDefinedFileAttributeView faView = Files.getFileAttributeView(
      f.toPath(), UserDefinedFileAttributeView.class);

    if (faView == null) {
      throw new IOException(
        "UserDefinedFileAttributeView not supported on file "
          + f.getAbsolutePath());
    }

    return getAttributeValue(faView, attributeName);

  }

  @Override
  public List<String> getExtendedFileAttributeNames(File f) throws IOException {

    Assert.notNull(f);
    UserDefinedFileAttributeView faView = Files.getFileAttributeView(
      f.toPath(), UserDefinedFileAttributeView.class);

    if (faView == null) {
      throw new IOException(
        "UserDefinedFileAttributeView not supported on file "
          + f.getAbsolutePath());
    }

    return faView.list();
  }

  @Override
  public void setChecksumAttribute(File f, String checksumValue)
    throws IOException {

    if (fileSupportsExtendedAttributes(f)) {
      setExtendedFileAttribute(f, STORM_ADLER32_CHECKSUM_ATTR_NAME,
        checksumValue);
    }

  }

  @Override
  public String getChecksumAttribute(File f) throws IOException {

    return getExtendedFileAttributeValue(f, STORM_ADLER32_CHECKSUM_ATTR_NAME);
  }

  @Override
  public boolean fileSupportsExtendedAttributes(File f) throws IOException {

    Assert.notNull(f);
    UserDefinedFileAttributeView faView = Files.getFileAttributeView(
      f.toPath(), UserDefinedFileAttributeView.class);

    return (faView != null);
  }

}
