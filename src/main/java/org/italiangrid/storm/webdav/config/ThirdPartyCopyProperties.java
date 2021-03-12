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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("tpc")
@Validated
public class ThirdPartyCopyProperties {

  @NotBlank(message = "tpc.tlsProtocol cannot be a blank string")
  String tlsProtocol;

  @Positive(message = "tpc.maxConnections must be a positive integer (i.e. > 0)")
  int maxConnections;

  @Positive(message = "tpc.maxConnectionsPerRoute must be a positive integer (i.e. > 0)")
  int maxConnectionsPerRoute;

  boolean verifyChecksum;

  @Positive(message = "tpc.reportDelaySecs must be a positive integer (i.e. > 0)")
  int reportDelaySecs;

  @Min(value = 4096, message = "tpc.httpClientSocketBufferSize must be > 4096")
  int httpClientSocketBufferSize = 8192;
  
  boolean useConscrypt = false;

  boolean enableTlsClientAuth = false;

  @Min(value = 0, message = "Timeout in seconds must be >= 0")
  int timeoutInSecs;

  @Positive(message = "tpc.progressReportThreadPoolSize must be a positive integer (i.e. > 0)")
  int progressReportThreadPoolSize;

  public String getTlsProtocol() {
    return tlsProtocol;
  }

  public void setTlsProtocol(String tlsProtocol) {
    this.tlsProtocol = tlsProtocol;
  }

  public int getMaxConnections() {
    return maxConnections;
  }

  public void setMaxConnections(int maxConnections) {
    this.maxConnections = maxConnections;
  }

  public boolean isVerifyChecksum() {
    return verifyChecksum;
  }

  public void setVerifyChecksum(boolean verifyChecksum) {
    this.verifyChecksum = verifyChecksum;
  }

  public int getReportDelaySecs() {
    return reportDelaySecs;
  }

  public void setReportDelaySecs(int reportDelaySecs) {
    this.reportDelaySecs = reportDelaySecs;
  }

  public int getHttpClientSocketBufferSize() {
    return httpClientSocketBufferSize;
  }

  public void setHttpClientSocketBufferSize(int httpClientSocketBufferSize) {
    this.httpClientSocketBufferSize = httpClientSocketBufferSize;
  }

  public boolean isUseConscrypt() {
    return useConscrypt;
  }

  public void setUseConscrypt(boolean useConscrypt) {
    this.useConscrypt = useConscrypt;
  }

  public boolean isEnableTlsClientAuth() {
    return enableTlsClientAuth;
  }

  public void setEnableTlsClientAuth(boolean enableTlsClientAuth) {
    this.enableTlsClientAuth = enableTlsClientAuth;
  }

  public int getMaxConnectionsPerRoute() {
    return maxConnectionsPerRoute;
  }

  public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
    this.maxConnectionsPerRoute = maxConnectionsPerRoute;
  }

  public int getTimeoutInSecs() {
    return timeoutInSecs;
  }

  public void setTimeoutInSecs(int timeoutInSecs) {
    this.timeoutInSecs = timeoutInSecs;
  }

  public int getProgressReportThreadPoolSize() {
    return progressReportThreadPoolSize;
  }

  public void setProgressReportThreadPoolSize(int progressReportThreadPoolSize) {
    this.progressReportThreadPoolSize = progressReportThreadPoolSize;
  }

}
