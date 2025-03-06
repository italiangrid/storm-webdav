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

  public static class AuthorizationServer {

    @NotBlank String name;

    @URL String issuer;

    @URL String jwkUri;

    boolean enforceAudienceChecks = false;

    List<String> audiences = new ArrayList<>();

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getJwkUri() {
      return jwkUri;
    }

    public void setJwkUri(String jwkUri) {
      this.jwkUri = jwkUri;
    }

    public String getIssuer() {
      return issuer;
    }

    public void setIssuer(String issuer) {
      this.issuer = issuer;
    }

    public void setAudiences(List<String> audiences) {
      this.audiences = audiences;
    }

    public List<String> getAudiences() {
      return audiences;
    }

    public void setEnforceAudienceChecks(boolean enforceAudienceChecks) {
      this.enforceAudienceChecks = enforceAudienceChecks;
    }

    public boolean isEnforceAudienceChecks() {
      return enforceAudienceChecks;
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
