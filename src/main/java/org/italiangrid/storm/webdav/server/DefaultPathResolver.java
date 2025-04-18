// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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

import io.opentelemetry.instrumentation.annotations.WithSpan;

public class DefaultPathResolver implements PathResolver {

  private final StorageAreaConfiguration saConfig;

  private static final Logger LOG = LoggerFactory.getLogger(DefaultPathResolver.class);

  private final NavigableMap<String, StorageAreaInfo> contextMap;

  @WithSpan
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

    if (context != null && !context.isEmpty()) {
      return path.replaceFirst(context, "");
    } else {
      return path;
    }
  }
  
  @WithSpan
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

  @WithSpan
  @Override
  public StorageAreaInfo resolveStorageArea(String pathInContext) {

    for (Map.Entry<String, StorageAreaInfo> e : contextMap.descendingMap().entrySet()) {

      if (pathInContext.startsWith(e.getKey())) {

        if (LOG.isDebugEnabled()) {
          LOG.debug(
              "{} matches with access point {}. Resolved storage area name: {}",
              pathInContext,
              e.getKey(),
              e.getValue().name());
        }

        return e.getValue();
      }
    }
    return null;
  }

  @WithSpan
  @Override
  public boolean pathExists(String pathInContext) {
    String resolvedPath = resolvePath(pathInContext);

    if (resolvedPath == null) {
      return false;
    }

    return Files.exists(Paths.get(resolvedPath), NOFOLLOW_LINKS);
  }

  @WithSpan
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
          LOG.debug(
              "{} matches with access point {}. Resolved path: {}",
              pathInContext,
              e.getKey(),
              resolvedPath);
        }

        return resolvedPath.toAbsolutePath();
      }
    }

    return null;
  }
}
