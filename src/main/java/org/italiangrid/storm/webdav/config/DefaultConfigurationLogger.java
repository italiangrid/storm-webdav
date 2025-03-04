// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class DefaultConfigurationLogger implements ConfigurationLogger {

  final ServiceConfiguration serviceConfiguration;
  final StorageAreaConfiguration saConfiguration;

  public DefaultConfigurationLogger(ServiceConfiguration sc,
    StorageAreaConfiguration saConf) {

    serviceConfiguration = sc;
    saConfiguration = saConf;
  }

  private void logServiceConfiguration(Logger logger) {

    logger.info("## WebDAV service configuration");
    logger.info("Storage area configuration dir: {}",
      serviceConfiguration.getSAConfigDir());
    logger.info("### Connector configuration");
    logger.info("HTTP port: {}", serviceConfiguration.getHTTPPort());
    logger.info("HTTPS port: {}", serviceConfiguration.getHTTPSPort());
    logger
      .info("Max connections: {}", serviceConfiguration.getMaxConnections());
    logger.info("Max connection queue size: {}",
      serviceConfiguration.getMaxQueueSize());
    logger.info("### TLS configuration");
    logger.info("Service certificate path: {}",
      serviceConfiguration.getCertificatePath());
    logger.info("Service private key path: {}",
      serviceConfiguration.getPrivateKeyPath());
    logger.info("Trust anchors directory: {}",
      serviceConfiguration.getTrustAnchorsDir());
    logger.info("Trust anchors refresh intervals (seconds): {}",
      serviceConfiguration.getTrustAnchorsRefreshIntervalInSeconds());
    
    logger.info("Client certificate authentication required: {}", 
        serviceConfiguration.requireClientCertificateAuthentication());
    logger.info("### VOMS Map files configuration");
    logger.info("VOMS Map files enabled: {}",
      serviceConfiguration.enableVOMapFiles());
    logger.info("VOMS Map files configuration directory: {}",
      serviceConfiguration.getVOMapFilesConfigDir());
    logger.info("VOMS Map files refresh interval (seconds): {}",
      serviceConfiguration.getVOMapFilesRefreshIntervalInSeconds());
    
    if (serviceConfiguration.isAuthorizationDisabled()) {
      logger
        .warn("\n\n\nAuthorization is DISABLED! "
          + "Do not use this in production!\n\n\n");
    }
  }

  private void logSAConfiguration(Logger logger) {

    logger.info("## Storage Areas configuration");
    for (StorageAreaInfo sa : saConfiguration.getStorageAreaInfo()) {
      logger.info("{}: {}", sa.name(), sa);
    }
  }

  @Override
  public void logConfiguration(Logger logger) {

    logServiceConfiguration(logger);
    logSAConfiguration(logger);
  }

}
