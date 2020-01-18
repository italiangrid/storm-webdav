/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.italiangrid.storm.webdav.tpc;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.isNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class StaticHostListLocalURLService implements LocalURLService {

  private final Set<String> serviceAliases;

  public StaticHostListLocalURLService(List<String> serviceAliases) {
    checkNotNull(serviceAliases, "serviceAliases must be non-null");
    checkArgument(!serviceAliases.isEmpty(), "serviceAliases must not be empty");
    this.serviceAliases = Sets.newHashSet(serviceAliases);
  }

  @Override
  public boolean isLocalURL(String url) {
    try {

      URI uri = new URI(url);

      if (isNull(uri.getScheme())) {
        return true;
      }

      return serviceAliases.contains(uri.getHost());

    } catch (URISyntaxException e) {
      throw new URLResolutionError(e);
    }
  }
}
