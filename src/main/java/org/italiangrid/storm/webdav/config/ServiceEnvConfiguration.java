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
package org.italiangrid.storm.webdav.config;

public enum ServiceEnvConfiguration implements ServiceConfiguration {
  INSTANCE;

  @Override
  public int getHTTPSPort() {

    return Integer.parseInt(ServiceConfigVariable.HTTPS_PORT.getValue());
  }

  @Override
  public int getHTTPPort() {

    return Integer.parseInt(ServiceConfigVariable.HTTP_PORT.getValue());
  }

  @Override
  public String getCertificatePath() {

    return ServiceConfigVariable.CERTIFICATE_PATH.getValue();
  }

  @Override
  public String getPrivateKeyPath() {

    return ServiceConfigVariable.PRIVATE_KEY_PATH.getValue();
  }

  @Override
  public String getTrustAnchorsDir() {

    return ServiceConfigVariable.TRUST_ANCHORS_DIR.getValue();
  }

  @Override
  public long getTrustAnchorsRefreshIntervalInSeconds() {

    return Long.parseLong(ServiceConfigVariable.TRUST_ANCHORS_REFRESH_INTERVAL
      .getValue());
  }

  @Override
  public int getMaxConnections() {

    return Integer.parseInt(ServiceConfigVariable.MAX_CONNECTIONS.getValue());
  }

  @Override
  public int getMaxQueueSize() {

    return Integer.parseInt(ServiceConfigVariable.MAX_QUEUE_SIZE.getValue());
  }

  @Override
  public int getConnectorMaxIdleTimeInMsec() {

    return Integer.parseInt(ServiceConfigVariable.CONNECTOR_MAX_IDLE_TIME
      .getValue());
  }

  @Override
  public String getSAConfigDir() {

    return ServiceConfigVariable.SA_CONFIG_DIR.getValue();
  }

  @Override
  public String getLogConfigurationPath() {

    return ServiceConfigVariable.LOG_CONFIGURATION.getValue();
  }

  @Override
  public String getAccessLogConfigurationPath() {

    return ServiceConfigVariable.ACCESS_LOG_CONFIGURATION.getValue();
  }

  @Override
  public boolean enableVOMapFiles() {

    return Boolean.parseBoolean(ServiceConfigVariable.VO_MAP_FILES_ENABLE
      .getValue());
  }

  @Override
  public String getVOMapFilesConfigDir() {

    return ServiceConfigVariable.VO_MAP_FILES_CONFIG_DIR.getValue();
  }

  @Override
  public long getVOMapFilesRefreshIntervalInSeconds() {

    return Long.parseLong(ServiceConfigVariable.VO_MAP_FILES_REFRESH_INTERVAL
      .getValue());
  }

}

enum ServiceConfigVariable {

  HTTPS_PORT("8443"), HTTP_PORT("8085"), CERTIFICATE_PATH(
    "/etc/grid-security/storm-webdav/hostcert.pem"), PRIVATE_KEY_PATH(
    "/etc/grid-security/storm-webdav/hostkey.pem"), TRUST_ANCHORS_DIR(
    "/etc/grid-security/certificates"), TRUST_ANCHORS_REFRESH_INTERVAL("86400"), MAX_CONNECTIONS(
    "300"), MAX_QUEUE_SIZE("900"), CONNECTOR_MAX_IDLE_TIME("30000"), SA_CONFIG_DIR(
    "/etc/storm-webdav/sa.d"), LOG_CONFIGURATION(null), ACCESS_LOG_CONFIGURATION(
    null), VO_MAP_FILES_ENABLE("false"), VO_MAP_FILES_CONFIG_DIR(
    "/etc/storm-webdav/voms-mapfiles.d"), VO_MAP_FILES_REFRESH_INTERVAL("21600");

  private String defaultValue;

  private ServiceConfigVariable(String value) {

    this.defaultValue = value;
  }

  protected String getDefaultValue() {

    return defaultValue;
  }

  public String getEnvKey() {

    return String.format("STORM_WEBDAV_%s", name());
  }

  public String getValue() {

    String val = System.getenv(getEnvKey());
    if (val == null) {
      return defaultValue;
    }

    return val;
  }

}
