// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.util;

public class Version {

  private Version() {}

  public static String version() {

    String version = Version.class.getPackage().getImplementationVersion();
    if (version == null) {
      return "N/A";
    }
    return version;
  }
}
