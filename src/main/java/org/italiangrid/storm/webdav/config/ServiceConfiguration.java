// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config;

public interface ServiceConfiguration {

  int getHTTPSPort();

  int getHTTPPort();

  String getCertificatePath();

  String getPrivateKeyPath();

  String getTrustAnchorsDir();

  String getLogConfigurationPath();

  String getAccessLogConfigurationPath();

  long getTrustAnchorsRefreshIntervalInSeconds();

  int getConnectorMaxIdleTimeInMsec();

  String getSAConfigDir();

  boolean enableVOMapFiles();

  String getVOMapFilesConfigDir();

  long getVOMapFilesRefreshIntervalInSeconds();

  boolean isAuthorizationDisabled();

  boolean requireClientCertificateAuthentication();

  boolean useConscrypt();

  boolean enableHttp2();

  String getTlsProtocol();
}
