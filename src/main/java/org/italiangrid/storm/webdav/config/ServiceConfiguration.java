/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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
