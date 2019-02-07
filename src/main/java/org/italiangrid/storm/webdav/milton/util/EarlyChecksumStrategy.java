package org.italiangrid.storm.webdav.milton.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.italiangrid.storm.webdav.checksum.Adler32ChecksumInputStream;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.utils.RangeCopyHelper;

public class EarlyChecksumStrategy implements ReplaceContentStrategy {

  private final ExtendedAttributesHelper attributesHelper;

  public EarlyChecksumStrategy(ExtendedAttributesHelper ah) {
    this.attributesHelper = ah;
  }


  @Override
  public void replaceContent(InputStream in, Long length, File targetFile) throws IOException {
    Adler32ChecksumInputStream cis = new Adler32ChecksumInputStream(in);

    if (RangeCopyHelper.rangeCopy(cis, targetFile, 0, length) != length) {
      throw new StoRMWebDAVError("Incomplete copy error!");
    }

    attributesHelper.setChecksumAttribute(targetFile, cis.getChecksumValue());
  }

}