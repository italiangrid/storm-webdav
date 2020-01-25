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
package org.italiangrid.storm.webdav.config;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.italiangrid.storm.webdav.error.StoRMIntializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class SAConfigurationParser implements StorageAreaConfiguration {

  private final Set<String> RESERVED_SA_NAMES = Sets.newHashSet("oauth", ".well-known", "actuator", "css", "js", "authn-info");

  private final ServiceConfiguration serviceConfig;

  private String PROPERTIES_FILENAME_SUFFIX = ".properties";

  private List<StorageAreaInfo> saInfos;

  public SAConfigurationParser(ServiceConfiguration sc) {

    serviceConfig = sc;

    final Logger log = LoggerFactory.getLogger(SAConfigurationParser.class);

    final String saConfDir = serviceConfig.getSAConfigDir();

    log.debug("Loading SA configuration from directory {}", saConfDir);

    File dir = new File(saConfDir);
    directorySanityChecks(dir);

    File[] saFiles = dir.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File file, String name) {
        if (RESERVED_SA_NAMES.contains(name) && name.endsWith(PROPERTIES_FILENAME_SUFFIX)) {
          log.warn("Skipping {} as it is a reserved storage area name");
        }
        return (!RESERVED_SA_NAMES.contains(name) && name.endsWith(PROPERTIES_FILENAME_SUFFIX));
      }
    });

    if (saFiles.length == 0) {
      String msg = String.format(
          "No storage area configuration files found in directory '%s'. Was looking for files ending in '%s'",
          dir.getAbsolutePath(), PROPERTIES_FILENAME_SUFFIX);
      throw new StoRMIntializationError(msg);
    }

    saInfos = new ArrayList<StorageAreaInfo>();

    for (File f : saFiles) {
      
      Properties p = new Properties();
      try {
        p.load(new FileReader(f));
      } catch (Exception e) {
        throw new StoRMIntializationError("Error reading properties: " + e.getMessage(), e);
      }

      OwnerStorageAreaInfo saInfo = ConfigFactory.create(OwnerStorageAreaInfo.class, p);
      saInfos.add(saInfo);

      log.debug("{} loaded: {}", f, saInfo);
    }
  }

  private void directorySanityChecks(File directory) {

    if (!directory.exists())
      throw new StoRMIntializationError(
          "Storage area configuration directory does not exist: " + directory.getAbsolutePath());

    if (!directory.isDirectory())
      throw new StoRMIntializationError("Storage area configuration directory is not a directory: "
          + directory.getAbsolutePath());

    if (!directory.canRead())
      throw new StoRMIntializationError(
          "Storage area configuration directory is not readable: " + directory.getAbsolutePath());

    if (!directory.canExecute())
      throw new StoRMIntializationError("Storage area configuration directory is not traversable: "
          + directory.getAbsolutePath());

  }

  @Override
  public List<StorageAreaInfo> getStorageAreaInfo() {

    return saInfos;
  }

}
