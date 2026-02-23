// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.fs;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumInputStream;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.utils.IOExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.MethodNotAllowedException;

@Component
public class DefaultFSStrategy implements FilesystemAccess {

  public static final Logger LOG = LoggerFactory.getLogger(DefaultFSStrategy.class);

  final ExtendedAttributesHelper attrsHelper;

  private final ObservationRegistry observationRegistry;

  public DefaultFSStrategy(
      ExtendedAttributesHelper helper, ObservationRegistry observationRegistry) {

    attrsHelper = helper;
    this.observationRegistry = observationRegistry;
  }

  @Override
  public File mkdir(File parentDirectory, String dirName) {

    Observation observation =
        Observation.createNotStarted("fs-strategy-mkdir", this.observationRegistry);
    return observation.observe(
        () -> {
          LOG.debug("mkdir: parent={}, dir={}", parentDirectory.getAbsolutePath(), dirName);

          File nd = new File(parentDirectory, dirName);

          if (!nd.mkdir()) {
            LOG.warn("mkdir did not create {}", nd.getAbsolutePath());
          }
          return nd;
        });
  }

  @Override
  public void rm(File f) throws IOException {

    Observation observation =
        Observation.createNotStarted("fs-strategy-rm", this.observationRegistry);
    observation.observeChecked(
        () -> {
          LOG.debug("rm: {}", f.getAbsolutePath());
          Files.delete(f.toPath());
        });
  }

  @Override
  public void mv(File source, File dest) {

    Observation observation =
        Observation.createNotStarted("fs-strategy-mv", this.observationRegistry);
    observation.observe(
        () -> {
          LOG.debug("mv: source={}, dest={}", source.getAbsolutePath(), dest.getAbsolutePath());

          try {

            // Overwrites the destination, if it exists
            Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

          } catch (IOException e) {
            throw IOExceptionHelper.getStoRMWebDAVError(e);
          }
        });
  }

  @Override
  public File[] ls(File dir, int limit) {

    LOG.debug("ls: dir={} limit={}", dir, limit);
    return null;
  }

  @Override
  public void cp(File source, File dest) {

    Observation observation =
        Observation.createNotStarted("fs-strategy-cp", this.observationRegistry);
    observation.observe(
        () -> {
          LOG.debug("cp: source={} target={}", source.getAbsolutePath(), dest.getAbsolutePath());

          try {

            if (source.isDirectory()) {

              FileUtils.copyDirectory(source, dest);

            } else {

              Files.copy(source.toPath(), dest.toPath());
            }

          } catch (IOException e) {
            throw IOExceptionHelper.getStoRMWebDAVError(e);
          }
        });
  }

  @Override
  public File create(File file, InputStream in) {

    Observation observation =
        Observation.createNotStarted("fs-strategy-create", this.observationRegistry);
    return observation.observe(
        () -> {
          LOG.debug("create: file={}", file.getAbsolutePath());

          if (file.isDirectory()) {
            throw new MethodNotAllowedException(
                HttpMethod.PUT,
                Set.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.valueOf("PROPFIND")));
          }

          try (OutputStream fos = Files.newOutputStream(file.toPath())) {

            Adler32ChecksumInputStream cis = new Adler32ChecksumInputStream(in);

            IOUtils.copy(cis, fos);
            attrsHelper.setChecksumAttribute(file, cis.getChecksumValue());

            return file;

          } catch (IOException e) {
            throw IOExceptionHelper.getStoRMWebDAVError(e);
          }
        });
  }
}
