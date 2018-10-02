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

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("storm")
public class ServiceConfigurationProperties implements ServiceConfiguration {

  public static class AuthorizationProperties {

    boolean disabled = false;

    public boolean isDisabled() {
      return disabled;
    }

    public void setDisabled(boolean disabled) {
      this.disabled = disabled;
    }
  }

  public static class ConnectorProperties {

    @Positive
    @Max(65536)
    int port = 8085;

    @Positive
    @Max(65536)
    int securePort = 8443;

    @Positive
    int maxConnections = 200;

    @Positive
    int maxQueueSize = 512;

    @Positive
    int maxIdleTimeMsec = 30000;

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public int getSecurePort() {
      return securePort;
    }

    public void setSecurePort(int securePort) {
      this.securePort = securePort;
    }

    public int getMaxConnections() {
      return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
      this.maxConnections = maxConnections;
    }

    public int getMaxQueueSize() {
      return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
      this.maxQueueSize = maxQueueSize;
    }

    public int getMaxIdleTimeMsec() {
      return maxIdleTimeMsec;
    }

    public void setMaxIdleTimeMsec(int maxIdleTimeMsec) {
      this.maxIdleTimeMsec = maxIdleTimeMsec;
    }
  }

  public static class TLSProperties {
    @NotBlank
    String certificatePath;

    @NotBlank
    String privateKeyPath;

    @NotBlank
    String trustAnchorsDir;

    @Positive
    long trustAnchorsRefreshIntervalSecs = 86400;

    boolean requireClientCert = true;

    public String getCertificatePath() {
      return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
      this.certificatePath = certificatePath;
    }

    public String getPrivateKeyPath() {
      return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
      this.privateKeyPath = privateKeyPath;
    }

    public String getTrustAnchorsDir() {
      return trustAnchorsDir;
    }

    public void setTrustAnchorsDir(String trustAnchorsDir) {
      this.trustAnchorsDir = trustAnchorsDir;
    }

    public long getTrustAnchorsRefreshIntervalSecs() {
      return trustAnchorsRefreshIntervalSecs;
    }

    public void setTrustAnchorsRefreshIntervalSecs(long trustAnchorsRefreshIntervalSecs) {
      this.trustAnchorsRefreshIntervalSecs = trustAnchorsRefreshIntervalSecs;
    }

    public boolean isRequireClientCert() {
      return requireClientCert;
    }

    public void setRequireClientCert(boolean requireClientCert) {
      this.requireClientCert = requireClientCert;
    }
  }

  public static class SaProperties {

    @NotBlank(message = "Storage area configuration directory cannot be empty")
    String configDir;

    public String getConfigDir() {
      return configDir;
    }

    public void setConfigDir(String configDir) {
      this.configDir = configDir;
    }

  }

  public static class VoMapFilesProperties {
    String configDir;
    boolean enabled;
    int refreshIntervalSec;

    public String getConfigDir() {
      return configDir;
    }

    public void setConfigDir(String configDir) {
      this.configDir = configDir;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public int getRefreshIntervalSec() {
      return refreshIntervalSec;
    }

    public void setRefreshIntervalSec(int refreshIntervalSec) {
      this.refreshIntervalSec = refreshIntervalSec;
    }
  }

  public static class AuthorizationServerProperties {

    boolean enabled = true;

    @NotBlank(message = "Authorization server issuer must not be blank")
    String issuer;

    @NotBlank(message = "Authorization server secret must not be blank")
    String secret;

    @Positive
    int maxTokenLifetimeSec = 43200;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getIssuer() {
      return issuer;
    }

    public void setIssuer(String issuer) {
      this.issuer = issuer;
    }

    public String getSecret() {
      return secret;
    }

    public void setSecret(String secret) {
      this.secret = secret;
    }

    public int getMaxTokenLifetimeSec() {
      return maxTokenLifetimeSec;
    }

    public void setMaxTokenLifetimeSec(int maxTokenLifetimeSec) {
      this.maxTokenLifetimeSec = maxTokenLifetimeSec;
    }
  }
  
  @Valid
  public static class VOMSProperties {
    
    @Valid
    public static class VOMSCacheProperties {

      boolean enabled;

      @Positive(message="The VOMS cache entry lifetime must be a positive integer")
      int entryLifetimeSec;

      public boolean isEnabled() {
        return enabled;
      }

      public void setEnabled(boolean enabled) {
        this.enabled = enabled;
      }

      public int getEntryLifetimeSec() {
        return entryLifetimeSec;
      }

      public void setEntryLifetimeSec(int entryLifetimeSec) {
        this.entryLifetimeSec = entryLifetimeSec;
      }
    }

    VOMSCacheProperties cache;

    public VOMSCacheProperties getCache() {
      return cache;
    }

    public void setCache(VOMSCacheProperties cache) {
      this.cache = cache;
    }

  }

  private AuthorizationProperties authz;

  private ConnectorProperties connector;

  private TLSProperties tls;

  private SaProperties sa;

  private VoMapFilesProperties voMapFiles;

  private AuthorizationServerProperties authzServer;

  private VOMSProperties voms;

  public AuthorizationProperties getAuthz() {
    return authz;
  }


  public void setAuthz(AuthorizationProperties authz) {
    this.authz = authz;
  }


  public TLSProperties getTls() {
    return tls;
  }


  public void setTls(TLSProperties tls) {
    this.tls = tls;
  }


  public ConnectorProperties getConnector() {
    return connector;
  }


  public void setConnector(ConnectorProperties connector) {
    this.connector = connector;
  }


  public SaProperties getSa() {
    return sa;
  }


  public void setSa(SaProperties sa) {
    this.sa = sa;
  }


  public VoMapFilesProperties getVoMapFiles() {
    return voMapFiles;
  }


  public void setVoMapFiles(VoMapFilesProperties voMapFiles) {
    this.voMapFiles = voMapFiles;
  }


  @Override
  public int getHTTPSPort() {

    return getConnector().getSecurePort();
  }


  @Override
  public int getHTTPPort() {

    return getConnector().getPort();
  }


  @Override
  public String getCertificatePath() {

    return getTls().getCertificatePath();
  }


  @Override
  public String getPrivateKeyPath() {

    return getTls().getPrivateKeyPath();
  }


  @Override
  public String getTrustAnchorsDir() {

    return getTls().getTrustAnchorsDir();
  }


  @Override
  public String getLogConfigurationPath() {
    return null;
  }


  @Override
  public String getAccessLogConfigurationPath() {
    return null;
  }


  @Override
  public long getTrustAnchorsRefreshIntervalInSeconds() {
    return getTls().getTrustAnchorsRefreshIntervalSecs();
  }


  @Override
  public int getMaxConnections() {
    return getConnector().getMaxConnections();
  }


  @Override
  public int getMaxQueueSize() {
    return getConnector().getMaxQueueSize();
  }


  @Override
  public int getConnectorMaxIdleTimeInMsec() {
    return getConnector().getMaxIdleTimeMsec();
  }


  @Override
  public String getSAConfigDir() {
    return getSa().getConfigDir();
  }


  @Override
  public boolean enableVOMapFiles() {
    return getVoMapFiles().isEnabled();
  }


  @Override
  public String getVOMapFilesConfigDir() {
    return getVoMapFiles().getConfigDir();
  }


  @Override
  public long getVOMapFilesRefreshIntervalInSeconds() {
    return getVoMapFiles().getRefreshIntervalSec();
  }


  @Override
  public boolean isAuthorizationDisabled() {
    return getAuthz().isDisabled();
  }


  @Override
  public boolean requireClientCertificateAuthentication() {
    return getTls().isRequireClientCert();
  }


  public AuthorizationServerProperties getAuthzServer() {
    return authzServer;
  }


  public void setAuthzServer(AuthorizationServerProperties authzServer) {
    this.authzServer = authzServer;
  }


  public VOMSProperties getVoms() {
    return voms;
  }


  public void setVoms(VOMSProperties voms) {
    this.voms = voms;
  }

}
