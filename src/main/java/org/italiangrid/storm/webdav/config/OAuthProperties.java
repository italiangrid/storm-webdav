/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import com.google.common.collect.Lists;

@Configuration
@ConfigurationProperties("oauth")
@Validated
public class OAuthProperties {

  public static class AuthorizationServer {

    @NotBlank
    String name;

    @URL
    String issuer;

    @URL
    String jwkUri;

    boolean enforceAudienceChecks = false;

    List<String> audiences = Lists.newArrayList();

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

  public void setEnableOidc(boolean enableOidc) {
    this.enableOidc = enableOidc;
  }
  
  public boolean isEnableOidc() {
    return enableOidc;
  }
}
