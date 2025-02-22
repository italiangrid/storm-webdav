/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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
package org.italiangrid.storm.webdav.server;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DefaultPathResolver implements PathResolver {

  private final StorageAreaConfiguration saConfig;

  private static final Logger LOG = LoggerFactory.getLogger(DefaultPathResolver.class);

  private final NavigableMap<String, StorageAreaInfo> contextMap;

  public DefaultPathResolver(StorageAreaConfiguration cfg) {

    this.saConfig = cfg;
    contextMap = new TreeMap<>();

    for (StorageAreaInfo sa : saConfig.getStorageAreaInfo()) {
      for (String ap : sa.accessPoints()) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Adding path mapping for sa {}: {} -> {}", sa.name(), ap, sa.rootPath());
        }
        contextMap.put(ap, sa);
      }
    }

  }

  protected String stripContextPath(String context, String path) {

    if (context != null && context.length() > 0) {
      return path.replaceFirst(context, "");
    } else {
      return path;
    }
  }


  @Override
  public String resolvePath(String pathInContext) {

    if (pathInContext == null) {
      return null;
    }

    Path p = getPath(pathInContext);

    if (p != null) {
      return p.toString();
    }

    return null;
  }

  @Override
  public StorageAreaInfo resolveStorageArea(String pathInContext) {

    for (Map.Entry<String, StorageAreaInfo> e : contextMap.descendingMap().entrySet()) {

      if (pathInContext.startsWith(e.getKey())) {

        if (LOG.isDebugEnabled()) {
          LOG.debug("{} matches with access point {}. Resolved storage area name: {}",
              pathInContext, e.getKey(), e.getValue().name());
        }

        return e.getValue();
      }
    }
    return null;
  }

  @Override
  public boolean pathExists(String pathInContext) {
    String resolvedPath = resolvePath(pathInContext);

    if (resolvedPath == null) {
      return false;
    }

    return Files.exists(Paths.get(resolvedPath), NOFOLLOW_LINKS);
  }

  @Override
  public Path getPath(String pathInContext) {

    if (pathInContext == null) {
      return null;
    }

    for (Map.Entry<String, StorageAreaInfo> e : contextMap.descendingMap().entrySet()) {

      if (pathInContext.startsWith(e.getKey())) {

        Path resolvedPath =
            Paths.get(e.getValue().rootPath(), stripContextPath(e.getKey(), pathInContext));

        if (LOG.isDebugEnabled()) {
          LOG.debug("{} matches with access point {}. Resolved path: {}", pathInContext, e.getKey(),
              resolvedPath);
        }

        return resolvedPath.toAbsolutePath();
      }
    }

    return null;
  }

}
