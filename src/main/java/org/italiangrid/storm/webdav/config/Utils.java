// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config;

import java.io.File;

public class Utils {

  private Utils() {

  }

  public static String joinPaths(String p1, String p2) {

    File joined = new File(p1, p2);
    return joined.getAbsolutePath();
  }
}
