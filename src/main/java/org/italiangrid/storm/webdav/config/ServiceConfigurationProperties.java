// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config;

import static org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.RedirectorProperties.ReplicaPoolProperties.ReplicaSelectionPolicy.RANDOM;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("storm")
@Validated
public class ServiceConfigurationProperties implements ServiceConfiguration {

  @Validated
  public static class TapeProperties {

    @Validated
    public static class TapeWellKnownProperties {

      @NotEmpty String source;

      public String getSource() {
        return source;
      }

      public void setSource(String source) {
        this.source = source;
      }
    }

    TapeWellKnownProperties wellKnown;

    public TapeWellKnownProperties getWellKnown() {
      return wellKnown;
    }

    public void setWellKnown(TapeWellKnownProperties wellKnown) {
      this.wellKnown = wellKnown;
    }
  }

  public enum ChecksumStrategy {
    NO_CHECKSUM,
    EARLY,
    LATE
  }

  @Validated
  public static class RedirectorProperties {

    @Validated
    public static class ReplicaEndpointProperties {

      URI endpoint;

      public URI getEndpoint() {
        return endpoint;
      }

      public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
      }

      @Override
      public String toString() {
        return "[endpoint=" + endpoint + "]";
      }

      @Override
      public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
        return result;
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj) {
          return true;
        } else if (obj == null) {
          return false;
        } else if (getClass() != obj.getClass()) {
          return false;
        }
        ReplicaEndpointProperties other = (ReplicaEndpointProperties) obj;
        if (endpoint == null) {
          if (other.endpoint != null) {
            return false;
          }
        } else if (!endpoint.equals(other.endpoint)) {
          return false;
        }
        return true;
      }
    }

    @Validated
    public static class ReplicaPoolProperties {

      public enum ReplicaSelectionPolicy {
        RANDOM
      }

      @NotEmpty List<ReplicaEndpointProperties> endpoints = new ArrayList<>();

      ReplicaSelectionPolicy policy = RANDOM;

      public List<ReplicaEndpointProperties> getEndpoints() {
        return endpoints;
      }

      public void setEndpoints(List<ReplicaEndpointProperties> endpoints) {
        this.endpoints = endpoints;
      }

      public ReplicaSelectionPolicy getPolicy() {
        return policy;
      }

      public void setPolicy(ReplicaSelectionPolicy policy) {
        this.policy = policy;
      }
    }

    boolean enabled = false;

    @Positive int maxTokenLifetimeSecs = 1200;

    ReplicaPoolProperties pool = new ReplicaPoolProperties();

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public int getMaxTokenLifetimeSecs() {
      return maxTokenLifetimeSecs;
    }

    public void setMaxTokenLifetimeSecs(int maxTokenLifetimeSecs) {
      this.maxTokenLifetimeSecs = maxTokenLifetimeSecs;
    }

    public ReplicaPoolProperties getPool() {
      return pool;
    }

    public void setPool(ReplicaPoolProperties pool) {
      this.pool = pool;
    }
  }

  @Validated
  public static class BufferProperties {

    @Min(value = 4096, message = "storm.buffer.file-buffer-size-bytes must be >= 4096")
    int fileBufferSizeBytes = 1048576;

    public int getFileBufferSizeBytes() {
      return fileBufferSizeBytes;
    }

    public void setFileBufferSizeBytes(int fileBufferSizeBytes) {
      this.fileBufferSizeBytes = fileBufferSizeBytes;
    }
  }

  public static class MacaroonFilterProperties {

    boolean enabled = true;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  public static class ChecksumFilterProperties {

    boolean enabled = true;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  @Validated
  public static class AuthorizationProperties {

    boolean disabled = false;

    @Valid List<FineGrainedAuthzPolicyProperties> policies = new ArrayList<>();

    public boolean isDisabled() {
      return disabled;
    }

    public void setDisabled(boolean disabled) {
      this.disabled = disabled;
    }

    public List<FineGrainedAuthzPolicyProperties> getPolicies() {
      return policies;
    }

    public void setPolicies(List<FineGrainedAuthzPolicyProperties> policies) {
      this.policies = policies;
    }
  }

  public static class ServerProperties {

    @Positive int maxIdleTimeMsec = 3600000;

    public int getMaxIdleTimeMsec() {
      return maxIdleTimeMsec;
    }

    public void setMaxIdleTimeMsec(int maxIdleTimeMsec) {
      this.maxIdleTimeMsec = maxIdleTimeMsec;
    }
  }

  public static class ConnectorProperties {

    @Positive
    @Max(65536)
    int port = 8085;

    @Positive
    @Max(65536)
    int securePort = 8443;

    @Positive int minConnections = 50;

    @Positive int maxConnections = 300;

    @Positive int maxQueueSize = 900;

    @Positive int maxIdleTimeMsec = 30000;

    int jettyAcceptors = -1;

    int jettySelectors = -1;

    @Positive
    @Min(4096)
    int outputBufferSizeBytes = 32 * 1024;

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

    public int getMinConnections() {
      return minConnections;
    }

    public void setMinConnections(int minConnections) {
      this.minConnections = minConnections;
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

    public void setJettyAcceptors(int jettyAcceptors) {
      this.jettyAcceptors = jettyAcceptors;
    }

    public int getJettyAcceptors() {
      return jettyAcceptors;
    }

    public void setJettySelectors(int jettySelectors) {
      this.jettySelectors = jettySelectors;
    }

    public int getJettySelectors() {
      return jettySelectors;
    }

    public int getOutputBufferSizeBytes() {
      return outputBufferSizeBytes;
    }

    public void setOutputBufferSizeBytes(int outputBufferSizeBytes) {
      this.outputBufferSizeBytes = outputBufferSizeBytes;
    }
  }

  public static class TLSProperties {
    @NotBlank String certificatePath;

    @NotBlank String privateKeyPath;

    @NotBlank String trustAnchorsDir;

    @Positive long trustAnchorsRefreshIntervalSecs = 86400;

    boolean requireClientCert = true;

    boolean useConscrypt = false;

    boolean enableHttp2 = false;

    @NotBlank String protocol = "TLS";

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

    public boolean isUseConscrypt() {
      return useConscrypt;
    }

    public void setUseConscrypt(boolean useConscrypt) {
      this.useConscrypt = useConscrypt;
    }

    public void setEnableHttp2(boolean enableHttp2) {
      this.enableHttp2 = enableHttp2;
    }

    public boolean isEnableHttp2() {
      return enableHttp2;
    }

    public String getProtocol() {
      return protocol;
    }

    public void setProtocol(String protocol) {
      this.protocol = protocol;
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

    @Positive int maxTokenLifetimeSec = 43200;

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

    public static class VOMSTrustStoreProperties {

      String dir;

      int refreshIntervalSec;

      public String getDir() {
        return dir;
      }

      public void setDir(String dir) {
        this.dir = dir;
      }

      public int getRefreshIntervalSec() {
        return refreshIntervalSec;
      }

      public void setRefreshIntervalSec(int refreshIntervalSec) {
        this.refreshIntervalSec = refreshIntervalSec;
      }
    }

    @Valid
    public static class VOMSCacheProperties {

      boolean enabled;

      @Positive(message = "The VOMS cache entry lifetime must be a positive integer")
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
    VOMSTrustStoreProperties trustStore;

    public VOMSCacheProperties getCache() {
      return cache;
    }

    public void setCache(VOMSCacheProperties cache) {
      this.cache = cache;
    }

    public VOMSTrustStoreProperties getTrustStore() {
      return trustStore;
    }

    public void setTrustStore(VOMSTrustStoreProperties trustStore) {
      this.trustStore = trustStore;
    }
  }

  private AuthorizationProperties authz = new AuthorizationProperties();

  private ChecksumFilterProperties checksumFilter;

  private MacaroonFilterProperties macaroonFilter;

  private ServerProperties server;

  private ConnectorProperties connector;

  private TLSProperties tls;

  private SaProperties sa;

  private VoMapFilesProperties voMapFiles;

  private AuthorizationServerProperties authzServer;

  private VOMSProperties voms;

  private String logConfigurationPath;

  private String accessLogConfigurationPath;

  private ChecksumStrategy checksumStrategy = ChecksumStrategy.EARLY;

  private BufferProperties buffer;

  private RedirectorProperties redirector;

  private TapeProperties tape;

  @NotEmpty private List<String> hostnames;

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

  public ServerProperties getServer() {
    return server;
  }

  public void setServer(ServerProperties server) {
    this.server = server;
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
    return logConfigurationPath;
  }

  public void setLogConfigurationPath(String logConfigurationPath) {
    this.logConfigurationPath = logConfigurationPath;
  }

  @Override
  public String getAccessLogConfigurationPath() {
    return accessLogConfigurationPath;
  }

  public void setAccessLogConfigurationPath(String accessLogConfigurationPath) {
    this.accessLogConfigurationPath = accessLogConfigurationPath;
  }

  @Override
  public long getTrustAnchorsRefreshIntervalInSeconds() {
    return getTls().getTrustAnchorsRefreshIntervalSecs();
  }

  @Override
  public int getMinConnections() {
    return getConnector().getMinConnections();
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
  public int getThreadPoolMaxIdleTimeInMsec() {
    return getServer().getMaxIdleTimeMsec();
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

  public List<String> getHostnames() {
    return hostnames;
  }

  public void setHostnames(List<String> hostnames) {
    this.hostnames = hostnames;
  }

  public ChecksumFilterProperties getChecksumFilter() {
    return checksumFilter;
  }

  public void setChecksumFilter(ChecksumFilterProperties checksumFilter) {
    this.checksumFilter = checksumFilter;
  }

  public MacaroonFilterProperties getMacaroonFilter() {
    return macaroonFilter;
  }

  public void setMacaroonFilter(MacaroonFilterProperties macaroonFilter) {
    this.macaroonFilter = macaroonFilter;
  }

  public ChecksumStrategy getChecksumStrategy() {
    return checksumStrategy;
  }

  public void setChecksumStrategy(ChecksumStrategy checksumStrategy) {
    this.checksumStrategy = checksumStrategy;
  }

  @Override
  public boolean useConscrypt() {
    return getTls().isUseConscrypt();
  }

  @Override
  public boolean enableHttp2() {
    return getTls().isEnableHttp2();
  }

  public BufferProperties getBuffer() {
    return buffer;
  }

  public void setBuffer(BufferProperties buffer) {
    this.buffer = buffer;
  }

  public RedirectorProperties getRedirector() {
    return redirector;
  }

  public void setRedirector(RedirectorProperties redirector) {
    this.redirector = redirector;
  }

  @Override
  public String getTlsProtocol() {
    return getTls().getProtocol();
  }

  public TapeProperties getTape() {
    return tape;
  }

  public void setTape(TapeProperties tape) {
    this.tape = tape;
  }
}
