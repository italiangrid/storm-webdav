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
package org.italiangrid.storm.webdav.spring;

import static java.util.Objects.isNull;
import static org.italiangrid.storm.webdav.server.TLSServerConnectorBuilder.CONSCRYPT_PROVIDER;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.time.Clock;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.conscrypt.OpenSSLProvider;
import org.italiangrid.storm.webdav.authn.PrincipalHelper;
import org.italiangrid.storm.webdav.authz.AuthorizationPolicyService;
import org.italiangrid.storm.webdav.authz.PathAuthzPolicyParser;
import org.italiangrid.storm.webdav.authz.pdp.DefaultPathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.InMemoryPolicyRepository;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPolicyRepository;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.OAuthProperties.AuthorizationServer;
import org.italiangrid.storm.webdav.config.SAConfigurationParser;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.fs.DefaultFSStrategy;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.MetricsFSStrategyWrapper;
import org.italiangrid.storm.webdav.fs.attrs.DefaultExtendedFileAttributesHelper;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.milton.util.EarlyChecksumStrategy;
import org.italiangrid.storm.webdav.milton.util.LateChecksumStrategy;
import org.italiangrid.storm.webdav.milton.util.MetricsReplaceContentStrategy;
import org.italiangrid.storm.webdav.milton.util.NoChecksumStrategy;
import org.italiangrid.storm.webdav.milton.util.ReplaceContentStrategy;
import org.italiangrid.storm.webdav.oauth.CompositeJwtDecoder;
import org.italiangrid.storm.webdav.oauth.authzserver.DefaultTokenIssuerService;
import org.italiangrid.storm.webdav.oauth.authzserver.TokenIssuerService;
import org.italiangrid.storm.webdav.oauth.authzserver.TokenIssuerServiceMetricsWrapper;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.LocallyIssuedJwtDecoder;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.SignedJwtTokenIssuer;
import org.italiangrid.storm.webdav.oauth.authzserver.web.AuthzServerMetadata;
import org.italiangrid.storm.webdav.oauth.utils.OidcConfigurationFetcher;
import org.italiangrid.storm.webdav.oauth.utils.PermissiveBearerTokenResolver;
import org.italiangrid.storm.webdav.oauth.utils.TrustedJwtDecoderCacheLoader;
import org.italiangrid.storm.webdav.oidc.ClientRegistrationCacheLoader;
import org.italiangrid.storm.webdav.server.DefaultPathResolver;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.server.util.CANLListener;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.italiangrid.storm.webdav.tpc.StaticHostListLocalURLService;
import org.italiangrid.storm.webdav.tpc.TpcPlainConnectionSocketFactory;
import org.italiangrid.storm.webdav.tpc.TpcSSLConnectionSocketFactory;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.storm.webdav.tpc.http.SuperLaxRedirectStrategy;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import eu.emi.security.authn.x509.CrlCheckingMode;
import eu.emi.security.authn.x509.NamespaceCheckingMode;
import eu.emi.security.authn.x509.OCSPCheckingMode;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.helpers.ssl.SSLTrustManager;
import eu.emi.security.authn.x509.impl.PEMCredential;

@Configuration
public class AppConfig implements TransferConstants {

  public static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);

  @Bean
  Clock systemClock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  SignedJwtTokenIssuer tokenIssuer(ServiceConfigurationProperties props,
      AuthorizationPolicyService policyService, PrincipalHelper helper, Clock clock) {
    return new DefaultJwtTokenIssuer(clock, props.getAuthzServer(), policyService, helper);
  }

  @Bean
  TokenIssuerService tokenIssuerService(ServiceConfigurationProperties props,
      SignedJwtTokenIssuer tokenIssuer, Clock clock, MetricRegistry registry) {

    TokenIssuerService service =
        new DefaultTokenIssuerService(props.getAuthzServer(), tokenIssuer, clock);

    return new TokenIssuerServiceMetricsWrapper(service, registry);
  }

  @Bean
  PEMCredential serviceCredential(ServiceConfiguration conf)
      throws KeyStoreException, CertificateException, IOException {

    return new PEMCredential(conf.getPrivateKeyPath(), conf.getCertificatePath(), null);
  }


  @Bean
  StorageAreaConfiguration storageAreaConfiguration(ServiceConfiguration conf) {
    return new SAConfigurationParser(conf);
  }


  @Bean
  ExtendedAttributesHelper extendedAttributesHelper() {

    return new DefaultExtendedFileAttributesHelper();
  }

  @Bean
  @Primary
  FilesystemAccess filesystemAccess() {

    return new MetricsFSStrategyWrapper(new DefaultFSStrategy(extendedAttributesHelper()),
        metricRegistry());

  }

  @Bean
  MetricRegistry metricRegistry() {

    MetricRegistry registry = new MetricRegistry();

    registry.registerAll("jvm.mem", new MemoryUsageGaugeSet());
    registry.registerAll("jvm.gc", new GarbageCollectorMetricSet());
    registry.registerAll("jvm.threads", new CachedThreadStatesGaugeSet(1, TimeUnit.MINUTES));
    return registry;
  }

  @Bean
  HealthCheckRegistry healthCheckRegistry() {

    return new HealthCheckRegistry();
  }



  @Bean
  X509CertChainValidatorExt canlCertChainValidator(ServiceConfiguration configuration) {

    CANLListener l = new org.italiangrid.storm.webdav.server.util.CANLListener();
    CertificateValidatorBuilder builder = new CertificateValidatorBuilder();

    long refreshInterval =
        TimeUnit.SECONDS.toMillis(configuration.getTrustAnchorsRefreshIntervalInSeconds());

    return builder.namespaceChecks(NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS_REQUIRE)
      .crlChecks(CrlCheckingMode.IF_VALID)
      .ocspChecks(OCSPCheckingMode.IGNORE)
      .lazyAnchorsLoading(false)
      .storeUpdateListener(l)
      .validationErrorListener(l)
      .trustAnchorsDir(configuration.getTrustAnchorsDir())
      .trustAnchorsUpdateInterval(refreshInterval)
      .build();

  }

  @Bean
  PathResolver pathResolver(ServiceConfiguration conf) {
    return new DefaultPathResolver(storageAreaConfiguration(conf));
  }


  @Bean
  ScheduledExecutorService tpcProgressReportEs(ThirdPartyCopyProperties props) {

    final int tpSize = props.getProgressReportThreadPoolSize();
    ThreadFactory namedThreadFactory =
        new ThreadFactoryBuilder().setNameFormat("tpc-progress-%d").setDaemon(true).build();

    return Executors.newScheduledThreadPool(tpSize, namedThreadFactory);
  }

  @Bean("tpcConnectionManager")
  HttpClientConnectionManager tpcClientConnectionManager(ThirdPartyCopyProperties props,
      ServiceConfiguration conf) throws KeyStoreException, CertificateException, IOException,
      NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
    PEMCredential serviceCredential = serviceCredential(conf);

    SSLTrustManager tm = new SSLTrustManager(canlCertChainValidator(conf));

    SSLContext ctx;

    if (props.isUseConscrypt()) {
      if (isNull(Security.getProvider(CONSCRYPT_PROVIDER))) {
        Security.addProvider(new OpenSSLProvider());
      }
      ctx = SSLContext.getInstance(props.getTlsProtocol(), CONSCRYPT_PROVIDER);
    } else {
      ctx = SSLContext.getInstance(props.getTlsProtocol());
    }

    if (props.isEnableTlsClientAuth()) {
      LOG.info("TLS client auth for third-party transfers: ENABLED");
      ctx.init(new KeyManager[] {serviceCredential.getKeyManager()}, new TrustManager[] {tm}, null);
    } else {
      LOG.info("TLS client auth for third-party transfers: DISABLED");
      ctx.init(null, new TrustManager[] {tm}, null);
    }

    ConnectionSocketFactory sf = TpcPlainConnectionSocketFactory.getSocketFactory();
    LayeredConnectionSocketFactory tlsSf = new TpcSSLConnectionSocketFactory(ctx);

    Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
      .register(HTTP, sf)
      .register(HTTPS, tlsSf)
      .register(DAV, sf)
      .register(DAVS, tlsSf)
      .build();

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);
    cm.setMaxTotal(props.getMaxConnections());
    cm.setDefaultMaxPerRoute(props.getMaxConnectionsPerRoute());
    return cm;
  }

  @Bean
  CloseableHttpClient transferClient(ThirdPartyCopyProperties props,
      @Qualifier("tpcConnectionManager") HttpClientConnectionManager cm) {

    ConnectionConfig connectionConfig =
        ConnectionConfig.custom().setBufferSize(props.getHttpClientSocketBufferSize()).build();

    int timeoutMsec = (int) TimeUnit.SECONDS.toMillis(props.getTimeoutInSecs());
    RequestConfig config = RequestConfig.custom()
      .setExpectContinueEnabled(false)
      .setConnectTimeout(timeoutMsec)
      .setConnectionRequestTimeout(timeoutMsec)
      .setSocketTimeout(timeoutMsec)
      .build();

    return HttpClients.custom()
      .setConnectionManager(cm)
      .setDefaultConnectionConfig(connectionConfig)
      .setDefaultRequestConfig(config)
      .setRedirectStrategy(SuperLaxRedirectStrategy.INSTANCE)
      .build();
  }

  @Bean
  @ConditionalOnProperty(name = "oauth.enable-oidc", havingValue = "true")
  ClientRegistrationRepository clientRegistrationRepository(
      OAuth2ClientProperties clientProperties, OAuthProperties props, ExecutorService executor) {


    ClientRegistrationCacheLoader loader =
        new ClientRegistrationCacheLoader(clientProperties, props, executor);


    LoadingCache<String, ClientRegistration> clients = CacheBuilder.newBuilder()
      .refreshAfterWrite(props.getRefreshPeriodMinutes(), TimeUnit.MINUTES)
      .build(loader);


    clientProperties.getRegistration().forEach((k, v) -> {
      LOG.info("Initializing OIDC provider: {}", k);
      try {
        clients.put(k, loader.load(k));
      } catch (Exception e) {
        LOG.warn("Error initializing OIDC provider {}: {}", k, e.getMessage());
        if (LOG.isDebugEnabled()) {
          LOG.warn("Error initializing OIDC provider {}: {}", k, e.getMessage(), e);
        }
      }
    });

    LOG.info("OpenID providers configuration will be refreshed every {} minutes",
        props.getRefreshPeriodMinutes());

    return (k) -> {
      try {
        return clients.get(k);
      } catch (ExecutionException e) {
        LOG.warn("Error fetching OIDC provider {}: {}", k, e.getMessage(), e);
        return null;
      }
    };
  }

  @Bean
  JwtDecoder jwtDecoder(OAuthProperties props, ServiceConfigurationProperties sProps,
      RestTemplateBuilder builder, OidcConfigurationFetcher fetcher, ExecutorService executor) {


    TrustedJwtDecoderCacheLoader loader =
        new TrustedJwtDecoderCacheLoader(sProps, props, builder, fetcher, executor);

    LoadingCache<String, JwtDecoder> decoders = CacheBuilder.newBuilder()
      .refreshAfterWrite(props.getRefreshPeriodMinutes(), TimeUnit.MINUTES)
      .build(loader);

    for (AuthorizationServer as : props.getIssuers()) {
      LOG.info("Initializing OAuth trusted issuer: {}", as.getIssuer());
      try {
        decoders.put(as.getIssuer(), loader.load(as.getIssuer()));
      } catch (Exception e) {
        LOG.warn("Error initializing trusted issuer: {}", e.getMessage());
        if (LOG.isDebugEnabled()) {
          LOG.warn("Error initializing trusted issuer: {}", e.getMessage(), e);
        }
      }
    }

    if (sProps.getAuthzServer().isEnabled()) {
      LOG.info("Initializing local JWT token issuer with issuer: {}",
          sProps.getAuthzServer().getIssuer());
      LocallyIssuedJwtDecoder d = new LocallyIssuedJwtDecoder(sProps.getAuthzServer());
      decoders.put(sProps.getAuthzServer().getIssuer(), d);
    }

    LOG.info("OAuth trusted issuer configuration will be refreshed every {} minutes",
        props.getRefreshPeriodMinutes());
    return new CompositeJwtDecoder(decoders);
  }

  @Bean
  LocalURLService localUrlService(ServiceConfigurationProperties props) {
    props.getHostnames().removeIf(String::isEmpty);
    return new StaticHostListLocalURLService(props.getHostnames());
  }

  @Bean
  AuthzServerMetadata metadata(ServiceConfigurationProperties props) {
    AuthzServerMetadata md = new AuthzServerMetadata();
    md.setIssuer(props.getAuthzServer().getIssuer());
    String tokenEndpoint = String.format("%s/oauth/token", props.getAuthzServer().getIssuer());
    md.setTokenEndpoint(tokenEndpoint);
    return md;
  }

  @Bean
  @ConditionalOnProperty(name = "storm.checksum-strategy", havingValue = "EARLY")
  ReplaceContentStrategy earlyChecksumStrategy(MetricRegistry registry,
      ExtendedAttributesHelper ah) {
    LOG.info("Checksum strategy: early");
    return new MetricsReplaceContentStrategy(registry, new EarlyChecksumStrategy(ah));
  }

  @Bean
  @ConditionalOnProperty(name = "storm.checksum-strategy", havingValue = "LATE")
  ReplaceContentStrategy lateChecksumStrategy(MetricRegistry registry,
      ExtendedAttributesHelper ah) {
    LOG.info("Checksum strategy: late");
    return new MetricsReplaceContentStrategy(registry, new LateChecksumStrategy(ah));
  }

  @Bean
  @ConditionalOnProperty(name = "storm.checksum-strategy", havingValue = "NO_CHECKSUM")
  ReplaceContentStrategy noChecksumStrategy(MetricRegistry registry) {
    LOG.warn("Checksum strategy: no checksum");
    return new MetricsReplaceContentStrategy(registry, new NoChecksumStrategy());
  }

  @Bean
  PathAuthorizationPolicyRepository pathAuthzPolicyRepository(PathAuthzPolicyParser parser) {
    return new InMemoryPolicyRepository(parser.parsePolicies());
  }

  @Bean
  PathAuthorizationPdp fineGrainedAuthzPdpd(PathAuthorizationPolicyRepository repo) {
    return new DefaultPathAuthorizationPdp(repo);
  }

  @Bean
  @ConditionalOnProperty(name = "oauth.enable-oidc", havingValue = "false")
  ClientRegistrationRepository emptyClientRegistrationRepository() {
    return (id) -> null;
  }


  @Bean
  @ConditionalOnProperty(name = "storm.redirector.enabled", havingValue = "true")
  BearerTokenResolver bearerTokenResolver(ServiceConfigurationProperties config) {
    return new PermissiveBearerTokenResolver();
  }

  @Bean
  PrincipalHelper principalHelper(ServiceConfigurationProperties config)
      throws MalformedURLException {
    return new PrincipalHelper(config);
  }
}


