package org.italiangrid.storm.webdav.tpc.utils;

import static java.util.Objects.isNull;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlHelper {

  private UrlHelper() {
    // no instantiation
  }

  public static boolean isRemoteUrl(String url) {
    try {

      URI uri = new URI(url);

      if (isNull(uri.getScheme())) {
        return false;
      }

      return true;

    } catch (URISyntaxException e) {
      return false;
    }

  }



}
