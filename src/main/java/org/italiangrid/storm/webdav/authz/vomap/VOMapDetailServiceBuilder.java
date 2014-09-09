package org.italiangrid.storm.webdav.authz.vomap;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VOMapDetailServiceBuilder {

  private static final String VOMAPFILE_SUFFIX = ".vomap";

  private static final Logger logger = LoggerFactory
    .getLogger(VOMapDetailServiceBuilder.class);

  private final ServiceConfiguration serviceConf;

  @Autowired
  public VOMapDetailServiceBuilder(ServiceConfiguration conf) {

    this.serviceConf = conf;
  }

  private void directorySanityChecks(File directory) {

    if (!directory.exists())
      throw new IllegalArgumentException(
        "VOMS map files configuration directory does not exists: "
          + directory.getAbsolutePath());

    if (!directory.isDirectory())
      throw new IllegalArgumentException(
        "VOMS map files configuration directory is not a directory: "
          + directory.getAbsolutePath());

    if (!directory.canRead())
      throw new IllegalArgumentException(
        "VOMS map files configuration directory is not readable: "
          + directory.getAbsolutePath());

    if (!directory.canExecute())
      throw new IllegalArgumentException(
        "VOMS map files configuration directory is not traversable: "
          + directory.getAbsolutePath());

  }

  public VOMapDetailsService build() {

    if (!serviceConf.enableVOMapFiles()) {
      logger.info("VOMS Map files disabled.");
      return null;
    }

    File configDir = new File(serviceConf.getVOMapFilesConfigDir());
    directorySanityChecks(configDir);

    File[] files = configDir.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {

        return name.endsWith(VOMAPFILE_SUFFIX);

      }
    });

    if (files.length == 0) {
      logger.warn(
        "No mapfiles found in {}. Was looking for files ending in {}",
        VOMAPFILE_SUFFIX);
      return null;
    }
    
    

    Set<VOMembershipProvider> providers = new HashSet<VOMembershipProvider>();
    for (File f : files) {
      try {
        String voName = FilenameUtils.removeExtension(f.getName());
        
        VOMembershipProvider prov = new DefaultVOMembershipProvider(
          voName, new MapfileVOMembershipSource(voName, f));

        providers.add(prov);

      } catch (Throwable t) {
        logger.error("Error parsing mapfile {}: {}", f.getAbsolutePath(),
          t.getMessage(), t);
        continue;
      }
    }

    return new DefaultVOMapDetailsService(providers,
      serviceConf.getVOMapFilesRefreshIntervalInSeconds());
  }

}
