package org.italiangrid.storm.webdav.utils;

public class ChecksumHelper {

  public final static int ADLER32_CHECKSUM_LENGTH = 8;

  public static String addLeadingZero(String checksum, int maxLength) {
    return ("0".repeat(maxLength) + checksum).substring(checksum.length());
  }
}
