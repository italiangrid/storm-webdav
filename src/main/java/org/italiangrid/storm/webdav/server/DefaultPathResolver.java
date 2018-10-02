/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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
import static java.util.Objects.isNull;

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

  private static final Logger logger = LoggerFactory.getLogger(DefaultPathResolver.class);

  private final NavigableMap<String, StorageAreaInfo> contextMap;

  public DefaultPathResolver(StorageAreaConfiguration cfg) {

    this.saConfig = cfg;
    contextMap = new TreeMap<String, StorageAreaInfo>();

    for (StorageAreaInfo sa : saConfig.getStorageAreaInfo()) {
      for (String ap : sa.accessPoints()) {
        logger.debug("Adding path mapping for sa {}: {} -> {}", sa.name(), ap, sa.rootPath());
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

    if (isNull(pathInContext)) {
      return null;
    }

    for (Map.Entry<String, StorageAreaInfo> e : contextMap.descendingMap().entrySet()) {

      if (pathInContext.startsWith(e.getKey())) {

        Path resolvedPath =
            Paths.get(e.getValue().rootPath(), stripContextPath(e.getKey(), pathInContext));

        if (logger.isDebugEnabled()) {
          logger.debug("{} matches with access point {}. Resolved path: {}", pathInContext,
              e.getKey(), resolvedPath);
        }

        return resolvedPath.toAbsolutePath().toString();
      }
    }

    return null;
  }

  @Override
  public StorageAreaInfo resolveStorageArea(String pathInContext) {

    for (Map.Entry<String, StorageAreaInfo> e : contextMap.descendingMap().entrySet()) {

      if (pathInContext.startsWith(e.getKey())) {

        if (logger.isDebugEnabled()) {
          logger.debug("{} matches with access point {}. Resolved storage area name: {}",
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

    if (isNull(resolvedPath)) {
      return false;
    }

    return Files.exists(Paths.get(resolvedPath), NOFOLLOW_LINKS);
  }


  


}
