package org.italiangrid.storm.webdav.config;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("tpc")
@Validated
public class ThirdPartyCopyProperties {

  @NotBlank(message="tpc.tlsProtocol cannot be a blank string")
  String tlsProtocol;

  @Positive(message="tpc.maxConnections must be a positive integer (i.e. > 0)")
  int maxConnections;

  boolean verifyChecksum;
  
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
  
}
