// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config;

public interface ServiceConfiguration {

  public int getHTTPSPort();

  public int getHTTPPort();

  public String getCertificatePath();

  public String getPrivateKeyPath();

  public String getTrustAnchorsDir();

  public String getLogConfigurationPath();

  public String getAccessLogConfigurationPath();

  public long getTrustAnchorsRefreshIntervalInSeconds();

  public int getMinConnections();

  public int getMaxConnections();

  public int getMaxQueueSize();

  public int getThreadPoolMaxIdleTimeInMsec();

  public int getConnectorMaxIdleTimeInMsec();

  public String getSAConfigDir();

  public boolean enableVOMapFiles();

  public String getVOMapFilesConfigDir();

  public long getVOMapFilesRefreshIntervalInSeconds();

  public boolean isAuthorizationDisabled();

  public boolean requireClientCertificateAuthentication();

  public boolean useConscrypt();

  public boolean enableHttp2();

  public String getTlsProtocol();
}
