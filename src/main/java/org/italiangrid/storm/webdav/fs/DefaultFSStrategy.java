/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumInputStream;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.io.Files;

@Component
public class DefaultFSStrategy implements FilesystemAccess {

  public static final Logger LOG = LoggerFactory
    .getLogger(DefaultFSStrategy.class);

  final ExtendedAttributesHelper attrsHelper;

  @Autowired
  public DefaultFSStrategy(ExtendedAttributesHelper helper) {

    attrsHelper = helper;
  }

  @Override
  public File mkdir(File parentDirectory, String dirName) {

    LOG.debug("mkdir: parent={}, dir={}", parentDirectory.getAbsolutePath(),
      dirName);

    File nd = new File(parentDirectory, dirName);

    if (!nd.mkdir()) {
      LOG.warn("mkdir did not create {}", nd.getAbsolutePath());
    }
    return nd;
  }

  @Override
  public boolean rm(File f) {

    LOG.debug("rm: {}", f.getAbsolutePath());
    return f.delete();
  }

  @Override
  public void mv(File source, File dest) {

    LOG.debug("mv: source={}, dest={}", source.getAbsolutePath(),
      dest.getAbsolutePath());
    try {

      Files.move(source, dest);

    } catch (IOException e) {
      throw new StoRMWebDAVError(e.getMessage(), e);
    }

  }

  @Override
  public File[] ls(File dir, int limit) {

    LOG.debug("ls: dir={} limit={}", dir, limit);
    return null;
  }

  @Override
  public void cp(File source, File dest) {

    LOG.debug("cp: source={} target={}", source.getAbsolutePath(),
      dest.getAbsolutePath());

    try {
      // TODO: default unix semantics implemented in guava copy,
      // if the file exists, its content is replaced.
      Files.copy(source, dest);
    } catch (IOException e) {

      throw new StoRMWebDAVError(e.getMessage(), e);
    }

  }

  @Override
  public File create(File file, InputStream in) {

    LOG.debug("create: file={}", file.getAbsolutePath());

    try {

      if (!file.createNewFile()) {
        LOG.warn("Create file on a file that already exists: {}",
          file.getAbsolutePath());
      }

      Adler32ChecksumInputStream cis = new Adler32ChecksumInputStream(in);

      IOUtils.copy(cis, new FileOutputStream(file));
      attrsHelper.setChecksumAttribute(file, cis.getChecksumValue());

      return file;

    } catch (IOException e) {
      throw new StoRMWebDAVError(e.getMessage(), e);
    }

  }
}
