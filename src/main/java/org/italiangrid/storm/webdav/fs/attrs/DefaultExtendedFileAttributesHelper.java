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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.isNull;
import static org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributes.STORM_MIGRATED_ATTR_NAME;
import static org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributes.STORM_RECALL_IN_PROGRESS_ATTR_NAME;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jnr.posix.FileStat;
import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;

public class DefaultExtendedFileAttributesHelper implements ExtendedAttributesHelper {

  public static final Logger LOG =
      LoggerFactory.getLogger(DefaultExtendedFileAttributesHelper.class);

  private static POSIX posix;

  public DefaultExtendedFileAttributesHelper() {
    posix = POSIXFactory.getPOSIX();
  }

  protected UserDefinedFileAttributeView getFileAttributeView(File f) throws IOException {

    UserDefinedFileAttributeView faView =
        Files.getFileAttributeView(f.toPath(), UserDefinedFileAttributeView.class);

    if (faView == null) {
      throw new IOException(
          "UserDefinedFileAttributeView not supported on file " + f.getAbsolutePath());
    }

    return faView;
  }

  protected String getAttributeValue(UserDefinedFileAttributeView view, String name)
      throws IOException {

    if (view.list().contains(name)) {
      ByteBuffer buffer = ByteBuffer.allocateDirect(view.size(name));
      view.read(name, buffer);
      buffer.flip();
      return StandardCharsets.UTF_8.decode(buffer).toString();
    } else {
      return "";
    }
  }

  @Override
  public void setExtendedFileAttribute(Path p, ExtendedAttributes name, String value)
      throws IOException {
    setExtendedFileAttribute(p.toFile(), name, value);
  }

  @Override
  public void setExtendedFileAttribute(File f, ExtendedAttributes name, String value)
      throws IOException {

    checkNotNull(f);

    UserDefinedFileAttributeView faView = getFileAttributeView(f);

    int bytes = faView.write(name.toString(), StandardCharsets.UTF_8.encode(value));
    if (bytes > 0) {
      LOG.debug("Setting '{}' extended attribute on file '{}': {} bytes written", name.toString(),
          f.getAbsolutePath(), bytes);
    }
  }

  @Override
  public String getExtendedFileAttributeValue(File f, ExtendedAttributes attributeName)
      throws IOException {

    checkNotNull(f);

    UserDefinedFileAttributeView faView = getFileAttributeView(f);

    return getAttributeValue(faView, attributeName.toString());

  }

  @Override
  public List<String> getExtendedFileAttributeNames(File f) throws IOException {

    checkNotNull(f);

    UserDefinedFileAttributeView faView =
        Files.getFileAttributeView(f.toPath(), UserDefinedFileAttributeView.class);

    if (faView == null) {
      throw new IOException(
          "UserDefinedFileAttributeView not supported on file " + f.getAbsolutePath());
    }

    return faView.list();
  }

  @Override
  public boolean fileSupportsExtendedAttributes(File f) throws IOException {

    checkNotNull(f);

    UserDefinedFileAttributeView faView =
        Files.getFileAttributeView(f.toPath(), UserDefinedFileAttributeView.class);

    return (faView != null);
  }

  @Override
  public FileStat stat(Path p) throws IOException {
    checkNotNull(p);
    return stat(p.toFile());
  }

  @Override
  public FileStat stat(File f) throws IOException {
    checkNotNull(f);
    return posix.stat(f.getAbsolutePath());
  }

  @Override
  public FileStatus getFileStatus(Path p) throws IOException {
    checkNotNull(p);
    return getFileStatus(p.toFile());
  }

  @Override
  public FileStatus getFileStatus(File f) throws IOException {
    checkNotNull(f);

    FileStat stat = stat(f);
    if (isNull(stat)) {
      throw new IOException("Unable to stat file " + f.toString());
    }
    if (stat.isDirectory()) {
      return FileStatus.DISK;
    }
    List<String> attrs = getExtendedFileAttributeNames(f);

    // check if file has been migrated to taoe
    boolean hasMigrated = attrs.contains(STORM_MIGRATED_ATTR_NAME.toString());
    // check if file is available with 0 latency
    boolean isOnline = !isStub(stat);
    if (isOnline) {
      // file is available, check if it has a copy on tape
      return hasMigrated ? FileStatus.DISK_AND_TAPE : FileStatus.DISK;
    }
    // file is a stub, no migrated attribute means an undefined situation
    if (!hasMigrated) {
      return FileStatus.UNDEFINED;
    }
    // file is on tape, check if a recall is in progress
    boolean isRecallInProgress = attrs.contains(STORM_RECALL_IN_PROGRESS_ATTR_NAME.toString());
    return isRecallInProgress ? FileStatus.TAPE_RECALL_IN_PROGRESS : FileStatus.TAPE;
  }

  @Override
  public boolean isStub(Path p) throws IOException {
    checkNotNull(p);
    return isStub(p.toFile());
  }

  @Override
  public boolean isStub(File f) throws IOException {
    checkNotNull(f);
    return isStub(stat(f));
  }

  @Override
  public boolean isStub(FileStat fs) throws IOException {
    checkNotNull(fs);
    return fs.blockSize() * fs.blocks() < fs.st_size();
  }
}
