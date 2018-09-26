package org.italiangrid.storm.webdav.checksum;

import java.io.OutputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;

public class Adler32ChecksumOutputStream extends CheckedOutputStream {

  public Adler32ChecksumOutputStream(OutputStream out) {
    super(out, new Adler32());
  }
  
  public String getChecksumValue() {
    return Long.toHexString(getChecksum().getValue());
  }

}
