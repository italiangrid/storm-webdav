// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumInputStream;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.utils.RangeCopyHelper;

public class LateChecksumStrategy implements ReplaceContentStrategy {

  private final ExtendedAttributesHelper attributesHelper;

  public LateChecksumStrategy(ExtendedAttributesHelper ah) {
    this.attributesHelper = ah;
  }

  @Override
  public void replaceContent(InputStream in, Long length, File targetFile) throws IOException {

    if (RangeCopyHelper.rangeCopy(in, targetFile, 0, length) != length) {
      throw new StoRMWebDAVError("Incomplete copy error!");
    }

    calculateChecksum(targetFile);
  }

  protected void calculateChecksum(File targetFile) {

    try (Adler32ChecksumInputStream cis =
        new Adler32ChecksumInputStream(new BufferedInputStream(new FileInputStream(targetFile)))) {

      byte[] buffer = new byte[8192];

      while (cis.read(buffer) != -1) {
        // do nothing, just read
      }

      attributesHelper.setChecksumAttribute(targetFile, cis.getChecksumValue());

    } catch (IOException e) {
      throw new StoRMWebDAVError(e);
    }
  }
}
