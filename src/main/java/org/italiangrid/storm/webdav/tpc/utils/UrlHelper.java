// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlHelper {

  private UrlHelper() {
    // no instantiation
  }

  public static boolean isRemoteUrl(String url) {
    try {

      URI uri = new URI(url);

      return uri.getScheme() != null;

    } catch (URISyntaxException e) {
      return false;
    }
  }
}
