// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.util.Assert;

public class StaticHostListLocalURLService implements LocalURLService {

  private final Set<String> serviceAliases;

  public StaticHostListLocalURLService(List<String> serviceAliases) {
    Objects.requireNonNull(serviceAliases, "serviceAliases must be non-null");
    Assert.notEmpty(serviceAliases, "serviceAliases must not be empty");
    this.serviceAliases = new HashSet<>(serviceAliases);
  }

  @Override
  public boolean isLocalURL(String url) {
    try {

      URI uri = new URI(url);

      if (uri.getScheme() == null || (uri.getHost() != null && uri.getHost().equals("localhost"))) {
        return true;
      }

      return serviceAliases.contains(uri.getHost());

    } catch (URISyntaxException e) {
      throw new URLResolutionError(e);
    }
  }
}
