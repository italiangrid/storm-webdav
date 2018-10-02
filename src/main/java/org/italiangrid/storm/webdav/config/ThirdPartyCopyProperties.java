/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

  boolean verifyChecksum;

  @Positive(message = "tpc.reportDelaySecs must be a positive integer (i.e. > 0)")
  int reportDelaySecs;

  @Min(value = 4096, message = "tpc.httpClientSocketBufferSize must be > 4096")
  int httpClientSocketBufferSize = 8192;

  @Min(value = 4096, message = "tpc.localFileBufferSize must be > 4096")
  int localFileBufferSize = 8192;

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

  public int getLocalFileBufferSize() {
    return localFileBufferSize;
  }

  public void setLocalFileBufferSize(int localFileBufferSize) {
    this.localFileBufferSize = localFileBufferSize;
  }

}
