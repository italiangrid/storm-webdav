// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.spring;

import static org.italiangrid.storm.webdav.server.TLSServerConnectorBuilder.CONSCRYPT_PROVIDER;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import eu.emi.security.authn.x509.CrlCheckingMode;
import eu.emi.security.authn.x509.NamespaceCheckingMode;
import eu.emi.security.authn.x509.OCSPCheckingMode;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.helpers.ssl.SSLTrustManager;
import eu.emi.security.authn.x509.impl.PEMCredential;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.time.Clock;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
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
import org.italiangrid.storm.webdav.tpc.TpcSchemePortResolver;
import org.italiangrid.storm.webdav.tpc.TpcTlsSocketStrategy;
import org.italiangrid.storm.webdav.tpc.http.DropAuthorizationHeaderExec;
import org.italiangrid.storm.webdav.tpc.http.SuperLaxRedirectStrategy;
import org.italiangrid.storm.webdav.web.PathConstants;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;

@Configuration
public class AppConfig {

  public static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);

  @Bean
  Clock systemClock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  SignedJwtTokenIssuer tokenIssuer(
      ServiceConfigurationProperties props,
      AuthorizationPolicyService policyService,
      PrincipalHelper helper,
      Clock clock) {
    return new DefaultJwtTokenIssuer(clock, props.getAuthzServer(), policyService, helper);
  }

  @Bean
  TokenIssuerService tokenIssuerService(
      ServiceConfigurationProperties props,
      SignedJwtTokenIssuer tokenIssuer,
      Clock clock,
      MetricRegistry registry) {

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

    return new MetricsFSStrategyWrapper(
        new DefaultFSStrategy(extendedAttributesHelper()), metricRegistry());
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
    return canlCertChainCustomValidator(
        configuration, b -> b.namespaceChecks(NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS_REQUIRE));
  }

  private X509CertChainValidatorExt canlCertChainCustomValidator(
      ServiceConfiguration configuration,
      UnaryOperator<CertificateValidatorBuilder> customiseValidatorBuilder) {

    CANLListener l = new org.italiangrid.storm.webdav.server.util.CANLListener();

    long refreshInterval =
        TimeUnit.SECONDS.toMillis(configuration.getTrustAnchorsRefreshIntervalInSeconds());

    CertificateValidatorBuilder builder =
        new CertificateValidatorBuilder()
            .crlChecks(CrlCheckingMode.IF_VALID)
            .ocspChecks(OCSPCheckingMode.IGNORE)
            .lazyAnchorsLoading(false)
            .storeUpdateListener(l)
            .validationErrorListener(l)
            .trustAnchorsDir(configuration.getTrustAnchorsDir())
            .trustAnchorsUpdateInterval(refreshInterval);

    return customiseValidatorBuilder.apply(builder).build();
  }

  @Bean
  PathResolver pathResolver(ServiceConfiguration conf) {
    return new DefaultPathResolver(storageAreaConfiguration(conf));
  }

  @Bean
  ScheduledExecutorService tpcProgressReportEs(ThirdPartyCopyProperties props) {

    final int tpSize = props.getProgressReportThreadPoolSize();
    CustomizableThreadFactory namedThreadFactory = new CustomizableThreadFactory("tpc-progress-%d");
    namedThreadFactory.setDaemon(true);

    return Executors.newScheduledThreadPool(tpSize, namedThreadFactory);
  }

  @Bean("tpcConnectionManager")
  HttpClientConnectionManager tpcClientConnectionManager(
      ThirdPartyCopyProperties props, ServiceConfiguration conf)
      throws KeyStoreException,
          CertificateException,
          IOException,
          NoSuchAlgorithmException,
          NoSuchProviderException,
          KeyManagementException {

    Http1Config customHttpConfig =
        Http1Config.custom().setBufferSize(props.getHttpClientSocketBufferSize()).build();
    HttpConnectionFactory<ManagedHttpClientConnection> connectionFactory =
        ManagedHttpClientConnectionFactory.builder().http1Config(customHttpConfig).build();

    ConnectionConfig connectionConfig =
        ConnectionConfig.custom()
            .setSocketTimeout(props.getTimeoutInSecs(), TimeUnit.SECONDS)
            .setConnectTimeout(props.getTimeoutInSecs(), TimeUnit.SECONDS)
            .build();

    PEMCredential serviceCredential = serviceCredential(conf);

    X509CertChainValidatorExt validator =
        canlCertChainCustomValidator(conf, b -> b.namespaceChecks(NamespaceCheckingMode.IGNORE));
    SSLTrustManager tm = new SSLTrustManager(validator);

    SSLContext ctx;

    if (props.isUseConscrypt()) {
      if (Security.getProvider(CONSCRYPT_PROVIDER) == null) {
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

    return PoolingHttpClientConnectionManagerBuilder.create()
        .setConnectionFactory(connectionFactory)
        .setDefaultConnectionConfig(connectionConfig)
        .setMaxConnPerRoute(props.getMaxConnectionsPerRoute())
        .setSchemePortResolver(new TpcSchemePortResolver())
        .setTlsSocketStrategy(new TpcTlsSocketStrategy(ctx))
        .build();
  }

  @Bean
  CloseableHttpClient transferClient(
      ThirdPartyCopyProperties props,
      @Qualifier("tpcConnectionManager") HttpClientConnectionManager cm) {

    RequestConfig config =
        RequestConfig.custom()
            .setExpectContinueEnabled(false)
            .setConnectionRequestTimeout(props.getTimeoutInSecs(), TimeUnit.SECONDS)
            .build();

    return HttpClients.custom()
        .setConnectionManager(cm)
        .setDefaultRequestConfig(config)
        .setRedirectStrategy(SuperLaxRedirectStrategy.INSTANCE)
        .addExecInterceptorAfter(
            ChainElement.REDIRECT.name(),
            "DropAuthorizationHeader",
            new DropAuthorizationHeaderExec(SuperLaxRedirectStrategy.INSTANCE))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "oauth.enable-oidc", havingValue = "true")
  ClientRegistrationRepository clientRegistrationRepository(
      OAuth2ClientProperties clientProperties, OAuthProperties props, ExecutorService executor) {

    ClientRegistrationCacheLoader loader =
        new ClientRegistrationCacheLoader(clientProperties, props, executor);

    LoadingCache<String, ClientRegistration> clients =
        CacheBuilder.newBuilder()
            .refreshAfterWrite(props.getRefreshPeriodMinutes(), TimeUnit.MINUTES)
            .build(loader);

    clientProperties
        .getRegistration()
        .forEach(
            (k, v) -> {
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

    LOG.info(
        "OpenID providers configuration will be refreshed every {} minutes",
        props.getRefreshPeriodMinutes());

    return k -> {
      try {
        return clients.get(k);
      } catch (ExecutionException e) {
        LOG.warn("Error fetching OIDC provider {}: {}", k, e.getMessage(), e);
        return null;
      }
    };
  }

  @Bean
  JwtDecoder jwtDecoder(
      OAuthProperties props,
      ServiceConfigurationProperties sProps,
      RestTemplateBuilder builder,
      OidcConfigurationFetcher fetcher,
      ExecutorService executor) {

    TrustedJwtDecoderCacheLoader loader =
        new TrustedJwtDecoderCacheLoader(sProps, props, builder, fetcher, executor);

    LoadingCache<String, JwtDecoder> decoders =
        CacheBuilder.newBuilder()
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
      LOG.info(
          "Initializing local JWT token issuer with issuer: {}",
          sProps.getAuthzServer().getIssuer());
      LocallyIssuedJwtDecoder d = new LocallyIssuedJwtDecoder(sProps.getAuthzServer());
      decoders.put(sProps.getAuthzServer().getIssuer(), d);
    }

    LOG.info(
        "OAuth trusted issuer configuration will be refreshed every {} minutes",
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
    md.setTokenEndpoint(props.getAuthzServer().getIssuer() + PathConstants.OAUTH_TOKEN_PATH);
    return md;
  }

  @Bean
  @ConditionalOnProperty(name = "storm.checksum-strategy", havingValue = "EARLY")
  ReplaceContentStrategy earlyChecksumStrategy(
      MetricRegistry registry, ExtendedAttributesHelper ah) {
    LOG.info("Checksum strategy: early");
    return new MetricsReplaceContentStrategy(registry, new EarlyChecksumStrategy(ah));
  }

  @Bean
  @ConditionalOnProperty(name = "storm.checksum-strategy", havingValue = "LATE")
  ReplaceContentStrategy lateChecksumStrategy(
      MetricRegistry registry, ExtendedAttributesHelper ah) {
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
    return id -> null;
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

  @Bean
  @ConditionalOnProperty(name = "spring.session.store-type", havingValue = "none")
  public SessionRepository<MapSession> sessionRepository() {
    return new MapSessionRepository(new HashMap<>());
  }

  @Bean
  @Primary
  public WebEndpointProperties customizeWebEndpointProperties(
      WebEndpointProperties webEndpointProperties) {
    webEndpointProperties.setBasePath(PathConstants.ACTUATOR_PATH);
    return webEndpointProperties;
  }
}
