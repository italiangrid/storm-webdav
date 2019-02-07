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
package org.italiangrid.storm.webdav.spring;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.italiangrid.storm.webdav.authz.AuthorizationPolicyService;
import org.italiangrid.storm.webdav.config.OAuthProperties;
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
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.LocallyIssuedJwtDecoder;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.SignedJwtTokenIssuer;
import org.italiangrid.storm.webdav.oauth.authzserver.web.AuthzServerMetadata;
import org.italiangrid.storm.webdav.server.DefaultPathResolver;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.server.util.CANLListener;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.italiangrid.storm.webdav.tpc.StaticHostListLocalURLService;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.storm.webdav.tpc.http.SuperLaxRedirectStrategy;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.Maps;

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
  public Clock systemClock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  public SignedJwtTokenIssuer tokenIssuer(ServiceConfigurationProperties props,
      AuthorizationPolicyService policyService, Clock clock) {
    return new DefaultJwtTokenIssuer(clock, props.getAuthzServer(), policyService);
  }

  @Bean
  public TokenIssuerService tokenIssuerService(ServiceConfigurationProperties props,
      SignedJwtTokenIssuer tokenIssuer, Clock clock) {
    return new DefaultTokenIssuerService(props.getAuthzServer(), tokenIssuer, clock);
  }

  @Bean
  public PEMCredential serviceCredential(ServiceConfiguration conf)
      throws KeyStoreException, CertificateException, IOException {

    return new PEMCredential(conf.getPrivateKeyPath(), conf.getCertificatePath(), null);
  }


  @Bean
  public StorageAreaConfiguration storageAreaConfiguration(ServiceConfiguration conf) {
    return new SAConfigurationParser(conf);
  }


  @Bean
  public ExtendedAttributesHelper extendedAttributesHelper() {

    return new DefaultExtendedFileAttributesHelper();
  }

  @Bean
  @Primary
  public FilesystemAccess filesystemAccess() {

    return new MetricsFSStrategyWrapper(new DefaultFSStrategy(extendedAttributesHelper()),
        metricRegistry());

  }

  @Bean
  public MetricRegistry metricRegistry() {

    return new MetricRegistry();
  }

  @Bean
  public HealthCheckRegistry healthCheckRegistry() {

    return new HealthCheckRegistry();
  }



  @Bean
  public X509CertChainValidatorExt canlCertChainValidator(ServiceConfiguration configuration) {

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
  public PathResolver pathResolver(ServiceConfiguration conf) {
    return new DefaultPathResolver(storageAreaConfiguration(conf));
  }


  @Bean
  public ScheduledExecutorService tpcProgressReportEs(ThirdPartyCopyProperties props) {
    return new ScheduledThreadPoolExecutor(4);
  }

  @Bean
  public CloseableHttpClient transferClient(ThirdPartyCopyProperties props,
      ServiceConfiguration conf) throws NoSuchAlgorithmException, KeyManagementException,
      KeyStoreException, CertificateException, IOException {

    PEMCredential serviceCredential = serviceCredential(conf);

    SSLTrustManager tm = new SSLTrustManager(canlCertChainValidator(conf));

    SSLContext ctx = SSLContext.getInstance(props.getTlsProtocol());

    ctx.init(new KeyManager[] {serviceCredential.getKeyManager()}, new TrustManager[] {tm}, null);

    ConnectionSocketFactory sf = PlainConnectionSocketFactory.getSocketFactory();
    LayeredConnectionSocketFactory tlsSf = new SSLConnectionSocketFactory(ctx);

    Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
      .register(HTTP, sf)
      .register(HTTPS, tlsSf)
      .register(DAV, sf)
      .register(DAVS, tlsSf)
      .build();

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);
    cm.setMaxTotal(props.getMaxConnections());

    ConnectionConfig connectionConfig =
        ConnectionConfig.custom().setBufferSize(props.getHttpClientSocketBufferSize()).build();

    return HttpClients.custom()
      .setConnectionManager(cm)
      .setDefaultConnectionConfig(connectionConfig)
      .setRedirectStrategy(SuperLaxRedirectStrategy.INSTANCE)
      .build();
  }


  @Bean
  public JwtDecoder jwtDecoder(OAuthProperties props, ServiceConfigurationProperties sProps) {

    Map<String, JwtDecoder> decoders = Maps.newHashMap();

    props.getIssuers().forEach(i -> {
      decoders.put(i.getIssuer(), JwtDecoders.fromOidcIssuerLocation(i.getIssuer()));
    });

    LocallyIssuedJwtDecoder d = new LocallyIssuedJwtDecoder(sProps.getAuthzServer());
    decoders.put(sProps.getAuthzServer().getIssuer(), d);

    return new CompositeJwtDecoder(decoders);

  }

  @Bean
  public LocalURLService localUrlService(ServiceConfigurationProperties props) {
    props.getHostnames().removeIf(String::isEmpty);
    return new StaticHostListLocalURLService(props.getHostnames());
  }

  @Bean
  public AuthzServerMetadata metadata(ServiceConfigurationProperties props) {
    AuthzServerMetadata md = new AuthzServerMetadata();
    md.setIssuer(props.getAuthzServer().getIssuer());
    String tokenEndpoint = String.format("%s/oauth/token", props.getAuthzServer().getIssuer());
    md.setTokenEndpoint(tokenEndpoint);
    return md;
  }

  @Bean
  @ConditionalOnProperty(name = "storm.checksum-strategy", havingValue = "EARLY")
  public ReplaceContentStrategy earlyChecksumStrategy(MetricRegistry registry,
      ExtendedAttributesHelper ah) {
    LOG.info("Checksum strategy: early");
    return new MetricsReplaceContentStrategy(registry, new EarlyChecksumStrategy(ah));
  }

  @Bean
  @ConditionalOnProperty(name = "storm.checksum-strategy", havingValue = "LATE")
  public ReplaceContentStrategy lateChecksumStrategy(MetricRegistry registry,
      ExtendedAttributesHelper ah) {
    LOG.info("Checksum strategy: late");
    return new MetricsReplaceContentStrategy(registry, new LateChecksumStrategy(ah));
  }
  
  @Bean
  @ConditionalOnProperty(name = "storm.checksum-strategy", havingValue = "NO_CHECKSUM")
  public ReplaceContentStrategy noChecksumStrategy(MetricRegistry registry) {
    LOG.warn("Checksum strategy: no checksum");
    return new MetricsReplaceContentStrategy(registry, new NoChecksumStrategy());
  }
}