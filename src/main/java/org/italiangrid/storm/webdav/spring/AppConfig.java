/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
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
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.italiangrid.storm.webdav.authz.vomap.VOMapDetailServiceBuilder;
import org.italiangrid.storm.webdav.authz.vomap.VOMapDetailsService;
import org.italiangrid.storm.webdav.config.SAConfigurationParser;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.ServiceEnvConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.fs.DefaultFSStrategy;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.MetricsFSStrategyWrapper;
import org.italiangrid.storm.webdav.fs.attrs.DefaultExtendedFileAttributesHelper;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.DefaultPathResolver;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.server.util.CANLListener;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

import eu.emi.security.authn.x509.CrlCheckingMode;
import eu.emi.security.authn.x509.NamespaceCheckingMode;
import eu.emi.security.authn.x509.OCSPCheckingMode;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.helpers.ssl.SSLTrustManager;
import eu.emi.security.authn.x509.impl.PEMCredential;

@Configuration
public class AppConfig implements TransferConstants {


  @Bean
  public PEMCredential serviceCredential()
      throws KeyStoreException, CertificateException, IOException {
    ServiceConfiguration conf = serviceConfiguration();

    return new PEMCredential(conf.getPrivateKeyPath(), conf.getCertificatePath(), null);
  }


  @Bean
  public ServiceConfiguration serviceConfiguration() {
    return ServiceEnvConfiguration.INSTANCE;
  }

  @Bean
  public StorageAreaConfiguration storageAreaConfiguration() {
    return new SAConfigurationParser(serviceConfiguration());
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
  public VOMapDetailsService vomsMapDetailService() {

    VOMapDetailServiceBuilder builder = new VOMapDetailServiceBuilder(serviceConfiguration());
    return builder.build();
  }

  @Bean
  public X509CertChainValidatorExt canlCertChainValidator() {

    ServiceConfiguration configuration = serviceConfiguration();

    CANLListener l = new org.italiangrid.storm.webdav.server.util.CANLListener();
    CertificateValidatorBuilder builder = new CertificateValidatorBuilder();

    long refreshInterval =
        TimeUnit.SECONDS.toMillis(configuration.getTrustAnchorsRefreshIntervalInSeconds());

    X509CertChainValidatorExt validator =
        builder.namespaceChecks(NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS_REQUIRE)
          .crlChecks(CrlCheckingMode.IF_VALID)
          .ocspChecks(OCSPCheckingMode.IGNORE)
          .lazyAnchorsLoading(false)
          .storeUpdateListener(l)
          .validationErrorListener(l)
          .trustAnchorsDir(configuration.getTrustAnchorsDir())
          .trustAnchorsUpdateInterval(refreshInterval)
          .build();

    return validator;
  }

  @Bean
  public PathResolver pathResolver() {
    return new DefaultPathResolver(storageAreaConfiguration());
  }

  @Bean
  public CloseableHttpClient transferClient(ThirdPartyCopyProperties props)
      throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException,
      CertificateException, IOException {

    PEMCredential serviceCredential = serviceCredential();

    SSLTrustManager tm = new SSLTrustManager(canlCertChainValidator());

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


    return HttpClients.custom().setConnectionManager(cm).build();
  }
}


