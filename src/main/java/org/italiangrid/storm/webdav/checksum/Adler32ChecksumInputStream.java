package org.italiangrid.storm.webdav.checksum;

import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

public class Adler32ChecksumInputStream extends CheckedInputStream {

  public Adler32ChecksumInputStream(InputStream in) {

    super(in, new Adler32());
  }

  public String getChecksumValue() {

    return Long.toHexString(getChecksum().getValue());
  }

}
