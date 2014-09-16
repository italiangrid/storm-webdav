/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.util.URIUtil;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceMapper implements PathResolver {

  private final StorageAreaConfiguration saConfig;

  private static final Logger logger = LoggerFactory
    .getLogger(ResourceMapper.class);

  private final HashMap<String, String> contextMap;

  public ResourceMapper(StorageAreaConfiguration cfg) {

    this.saConfig = cfg;
    contextMap = new HashMap<String, String>();

    for (StorageAreaInfo sa : saConfig.getStorageAreaInfo()) {
      for (String ap : sa.accessPoints()) {
        logger.debug("Adding path mapping for sa {}: {} -> {}", sa.name(), ap,
          sa.rootPath());
        contextMap.put(ap, sa.rootPath());
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

    for (Map.Entry<String, String> e : contextMap.entrySet()) {
      if (pathInContext.startsWith(e.getKey())) {

        String resolvedPath = URIUtil.addPaths(e.getValue(),
          stripContextPath(e.getKey(), pathInContext));

        if (logger.isDebugEnabled()) {
          logger.debug("{} matches with access point {}. Resolved path: {}",
            pathInContext, e.getKey(), resolvedPath);
        }

        return resolvedPath;
      }
    }

    return null;
  }

}