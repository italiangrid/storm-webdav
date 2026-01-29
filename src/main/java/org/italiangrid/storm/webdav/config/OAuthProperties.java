// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("oauth")
@Validated
public class OAuthProperties {

  public static record AuthorizationServer(
      @NotBlank String name,
      @URL String issuer,
      @URL String jwkUri,
      boolean enforceAudienceChecks,
      List<String> audiences) {
    public AuthorizationServer {
      if (audiences == null) {
        audiences = new ArrayList<>();
      }
    }
  }

  List<AuthorizationServer> issuers;

  boolean enableOidc = false;

  @Min(value = 1, message = "The refresh period must be a positive integer")
  int refreshPeriodMinutes = 60;

  @Min(value = 1, message = "The refresh timeout must be a positive integer")
  int refreshTimeoutSeconds = 30;

  public List<AuthorizationServer> getIssuers() {
    return issuers;
  }

  public void setIssuers(List<AuthorizationServer> issuers) {
    this.issuers = issuers;
  }

  public int getRefreshPeriodMinutes() {
    return refreshPeriodMinutes;
  }

  public void setRefreshPeriodMinutes(int refreshPeriodMinutes) {
    this.refreshPeriodMinutes = refreshPeriodMinutes;
  }

  public int getRefreshTimeoutSeconds() {
    return refreshTimeoutSeconds;
  }

  public void setRefreshTimeoutSeconds(int refreshTimeoutSeconds) {
    this.refreshTimeoutSeconds = refreshTimeoutSeconds;
  }

  public void setEnableOidc(boolean enableOidc) {
    this.enableOidc = enableOidc;
  }

  public boolean isEnableOidc() {
    return enableOidc;
  }
}
