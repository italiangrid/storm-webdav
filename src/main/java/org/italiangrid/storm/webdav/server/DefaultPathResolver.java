// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.fs.Libc;
import org.italiangrid.storm.webdav.fs.Locality;
import org.italiangrid.storm.webdav.fs.Stat;
import org.italiangrid.storm.webdav.fs.attrs.DefaultExtendedFileAttributesHelper;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPathResolver implements PathResolver {

  private final StorageAreaConfiguration saConfig;

  private final ExtendedAttributesHelper attributesHelper;

  private static final Logger LOG = LoggerFactory.getLogger(DefaultPathResolver.class);

  String osName = System.getProperty("os.name");

  private final NavigableMap<String, StorageAreaInfo> contextMap;

  public DefaultPathResolver(
      StorageAreaConfiguration cfg, ExtendedAttributesHelper attributesHelper) {

    this.saConfig = cfg;
    this.attributesHelper = attributesHelper;
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

  @Override
  public boolean isStub(String pathInContext) {
    String resolvedPath = resolvePath(pathInContext);
    if (resolvedPath != null) {
      File f = new File(resolvedPath);
      if (f.isFile()) {
        if (osName.startsWith("Linux")) {
          Stat stat = new Stat();
          Libc.INSTANCE.stat(resolvedPath, stat);
          return stat.st_blocks.longValue() * 512 < f.length();
        } else if (osName.startsWith("Mac")) {
          try {
            Process process = Runtime.getRuntime().exec("stat -f %b " + resolvedPath);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
            long statBlockSize = Long.parseLong(reader.readLine());
            return statBlockSize * 512 < f.length();
          } catch (IOException e) {
            LOG.warn("Error getting block size: " + e.getMessage());
          }
        }
      }
    }
    return false;
  }

  @Override
  public Locality getLocality(String pathInContext) {
    if (!resolveStorageArea(pathInContext).tapeEnabled()) {
      return Locality.DISK;
    }
    Path filePath = getPath(pathInContext);
    String migratedAttribute = null;
    try {
      migratedAttribute = attributesHelper.getMigratedAttribute(filePath);
    } catch (IOException e) {
      LOG.warn(
          "Impossible getting migrated extended attribute of {}: {}",
          pathInContext,
          e.getMessage(),
          e);
    }
    if (isStub(pathInContext)) {
      if (migratedAttribute != null) {
        return Locality.TAPE;
      } else {
        LOG.warn(
            "The file {} appears lost, check stubbification and presence of {}{} xattr",
            filePath,
            DefaultExtendedFileAttributesHelper.USER_NAMESPACE,
            DefaultExtendedFileAttributesHelper.STORM_MIGRATED_ATTR_NAME);
        return Locality.UNAVAILABLE; // Undefined state
      }
    } else {
      if (migratedAttribute != null) {
        return Locality.DISK_AND_TAPE;
      } else {
        return Locality.DISK;
      }
    }
  }
}
