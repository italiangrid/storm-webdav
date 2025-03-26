// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.aeonbits.owner.ConfigFactory;
import org.italiangrid.storm.webdav.error.StoRMIntializationError;
import org.italiangrid.storm.webdav.fs.Libc;
import org.italiangrid.storm.webdav.fs.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SAConfigurationParser implements StorageAreaConfiguration {

  final Logger log = LoggerFactory.getLogger(SAConfigurationParser.class);

  private static final Set<String> RESERVED_SA_NAMES = Set.of(".storm-webdav", ".well-known");

  private final ServiceConfiguration serviceConfig;

  private static final String PROPERTIES_FILENAME_SUFFIX = ".properties";

  private List<StorageAreaInfo> saInfos;

  private boolean statAlreadyChecked = false;

  public SAConfigurationParser(ServiceConfiguration sc) {

    serviceConfig = sc;

    final String saConfDir = serviceConfig.getSAConfigDir();

    log.debug("Loading SA configuration from directory {}", saConfDir);

    File dir = new File(saConfDir);
    directorySanityChecks(dir);

    File[] saFiles =
        dir.listFiles(
            (file, name) -> {
              if (RESERVED_SA_NAMES.contains(name) && name.endsWith(PROPERTIES_FILENAME_SUFFIX)) {
                log.warn("Skipping {} as it is a reserved storage area name", name);
              }
              return (!RESERVED_SA_NAMES.contains(name)
                  && name.endsWith(PROPERTIES_FILENAME_SUFFIX));
            });

    if (saFiles.length == 0) {
      String msg =
          String.format(
              "No storage area configuration files found in directory '%s'. Was looking for files ending in '%s'",
              dir.getAbsolutePath(), PROPERTIES_FILENAME_SUFFIX);
      throw new StoRMIntializationError(msg);
    }

    saInfos = new ArrayList<>();

    for (File f : saFiles) {

      Properties p = new Properties();
      try (FileReader fr = new FileReader(f)) {
        p.load(fr);
      } catch (Exception e) {
        throw new StoRMIntializationError("Error reading properties: " + e.getMessage(), e);
      }

      OwnerStorageAreaInfo saInfo = ConfigFactory.create(OwnerStorageAreaInfo.class, p);
      if (!statAlreadyChecked && saInfo.tapeEnabled()) {
        checkStat(f);
      }
      saInfos.add(saInfo);

      log.debug("{} loaded: {}", f, saInfo);
    }
  }

  private void directorySanityChecks(File directory) {

    if (!directory.exists())
      throw new StoRMIntializationError(
          "Storage area configuration directory does not exist: " + directory.getAbsolutePath());

    if (!directory.isDirectory())
      throw new StoRMIntializationError(
          "Storage area configuration directory is not a directory: "
              + directory.getAbsolutePath());

    if (!directory.canRead())
      throw new StoRMIntializationError(
          "Storage area configuration directory is not readable: " + directory.getAbsolutePath());

    if (!directory.canExecute())
      throw new StoRMIntializationError(
          "Storage area configuration directory is not traversable: "
              + directory.getAbsolutePath());
  }

  @Override
  public List<StorageAreaInfo> getStorageAreaInfo() {

    return saInfos;
  }

  private void checkStat(File saFile) {
    try {
      if (System.getProperty("os.name").startsWith("Linux")) {
        Process process = Runtime.getRuntime().exec("stat -c %b " + saFile.getPath());
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        Stat stat = new Stat();
        Libc.INSTANCE.stat(saFile.getPath(), stat);
        long statBlockSize = Long.parseLong(reader.readLine());
        long jnaBlockSize = stat.st_blocks.longValue();
        if (statBlockSize != jnaBlockSize) {
          String msg =
              String.format(
                  "Cannot start StoRM WebDAV because block size read from stat (%d) is different from the one read with JNA wrapper (%d)",
                  statBlockSize, jnaBlockSize);
          throw new StoRMIntializationError(msg);
        }
      } else {
        log.warn("Cannot check stat because you are on an unsupported platform");
      }
      statAlreadyChecked = true;
    } catch (IOException e) {
      throw new StoRMIntializationError("Error checking block size: " + e.getMessage());
    }
  }
}
