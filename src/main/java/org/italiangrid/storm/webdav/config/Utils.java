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
