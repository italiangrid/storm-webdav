// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.fs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumInputStream;
import org.italiangrid.storm.webdav.error.SameFileError;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.MethodNotAllowedException;

import io.opentelemetry.instrumentation.annotations.WithSpan;

@Component
public class DefaultFSStrategy implements FilesystemAccess {

  public static final Logger LOG = LoggerFactory.getLogger(DefaultFSStrategy.class);

  final ExtendedAttributesHelper attrsHelper;

  public DefaultFSStrategy(ExtendedAttributesHelper helper) {

    attrsHelper = helper;
  }

  @WithSpan
  @Override
  public File mkdir(File parentDirectory, String dirName) {

    LOG.debug("mkdir: parent={}, dir={}", parentDirectory.getAbsolutePath(), dirName);

    File nd = new File(parentDirectory, dirName);

    if (!nd.mkdir()) {
      LOG.warn("mkdir did not create {}", nd.getAbsolutePath());
    }
    return nd;
  }

  @WithSpan
  @Override
  public void rm(File f) throws IOException {

    LOG.debug("rm: {}", f.getAbsolutePath());
    Files.delete(f.toPath());
  }

  @WithSpan
  @Override
  public void mv(File source, File dest) {

    LOG.debug("mv: source={}, dest={}", source.getAbsolutePath(), dest.getAbsolutePath());

    try {

      if (source.getCanonicalPath().equals(dest.getCanonicalPath())) {
        throw new SameFileError("Source and destination files are the same");
      }

      // Overwrites the destination, if it exists
      Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

    } catch (IOException e) {
      throw new StoRMWebDAVError(e.getMessage(), e);
    }
  }

  @WithSpan
  @Override
  public File[] ls(File dir, int limit) {

    LOG.debug("ls: dir={} limit={}", dir, limit);
    return null;
  }

  @WithSpan
  @Override
  public void cp(File source, File dest) {

    LOG.debug("cp: source={} target={}", source.getAbsolutePath(), dest.getAbsolutePath());

    try {

      if (source.getCanonicalPath().equals(dest.getCanonicalPath())) {
        throw new SameFileError("Source and destination files are the same");
      }

      if (source.isDirectory()) {

        FileUtils.copyDirectory(source, dest);

      } else {

        Files.copy(source.toPath(), dest.toPath());
      }

    } catch (IOException e) {

      throw new StoRMWebDAVError(e.getMessage(), e);
    }
  }

  @WithSpan
  @Override
  public File create(File file, InputStream in) {

    LOG.debug("create: file={}", file.getAbsolutePath());

    if (file.isDirectory()) {
      throw new MethodNotAllowedException(
          HttpMethod.PUT, Set.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.valueOf("PROPFIND")));
    }

    try (FileOutputStream fos = new FileOutputStream(file)) {

      Adler32ChecksumInputStream cis = new Adler32ChecksumInputStream(in);

      IOUtils.copy(cis, fos);
      attrsHelper.setChecksumAttribute(file, cis.getChecksumValue());

      return file;

    } catch (IOException e) {

      LOG.error(e.getMessage(), e);
      throw new StoRMWebDAVError(e.getMessage(), e);
    }
  }
}
