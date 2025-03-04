// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.utils;

public class ChecksumHelper {

  public static final int ADLER32_CHECKSUM_LENGTH = 8;

  public static String addLeadingZero(String checksum, int maxLength) {
    return ("0".repeat(maxLength) + checksum).substring(checksum.length());
  }

  private ChecksumHelper() {}
}
