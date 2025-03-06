// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server;

import com.codahale.metrics.MetricRegistry;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.helpers.ssl.SSLTrustManager;
import eu.emi.security.authn.x509.impl.PEMCredential;
import io.dropwizard.metrics.jetty12.InstrumentedConnectionFactory;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.conscrypt.OpenSSLProvider;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * A builder that configures a Jetty server TLS connector integrated with CANL {@link
 * X509CertChainValidatorExt} certificate validation services.
 */
public class TLSServerConnectorBuilder {

  /** Conscrypt provider name. */
  public static final String CONSCRYPT_PROVIDER = "Conscrypt";

  /** Default service certificate file. */
  public static final String DEFAULT_CERTIFICATE_FILE = "/etc/grid-security/hostcert.pem";

  /** Default service certificate private key file. */
  public static final String DEFAULT_CERTIFICATE_KEY_FILE = "/etc/grid-security/hostcert.pem";

  /** The port for this connector. */
  private int port;

  /** The certificate file location. */
  private String certificateFile = DEFAULT_CERTIFICATE_FILE;

  /** The certificate private key file location. */
  private String certificateKeyFile = DEFAULT_CERTIFICATE_KEY_FILE;

  /** The password to decrypt the certificate private key file. */
  private char[] certicateKeyPassword = null;

  /** The certificate validator used by this connector builder. */
  private final X509CertChainValidatorExt certificateValidator;

  /** Whether client auth will be required for this connector. */
  private boolean tlsNeedClientAuth = false;

  /** Whether cluent auth is supported for this connector. */
  private boolean tlsWantClientAuth = true;

  /** Supported SSL protocols. */
  private String[] includeProtocols;

  /** Disabled SSL protocols. */
  private String[] excludeProtocols;

  /** Supported cipher suites. */
  private String[] includeCipherSuites;

  /** Disabled cipher suites. */
  private String[] excludeCipherSuites;

  /** The HTTP configuration for the connector being created. */
  private HttpConfiguration httpConfiguration;

  /** The key manager to use for the connector being created. */
  private KeyManager keyManager;

  /** The server for which the connector is being created. */
  private final Server server;

  /** The metric name to associate to the connector being built. */
  private String metricName;

  /** The metric registry. */
  private MetricRegistry registry;

  /** Whether the Conscrypt provider should be used instead of the default JSSE implementation */
  private boolean useConscrypt = false;

  /** Whether HTTP/2 should be configured */
  private boolean enableHttp2 = false;

  /** Which TLS protocol string should be used */
  private String tlsProtocol = "TLSv1.2";

  /** Custom TLS hostname verifier */
  private HostnameVerifier hostnameVerifier = null;

  /** Disable JSSE hostname verification */
  private boolean disableJsseHostnameVerification = false;

  /** Number of acceptors threads for the connector */
  private int acceptors = -1;

  /** Number of selector threads for the connector */
  private int selectors = -1;

  /**
   * Returns an instance of the {@link TLSServerConnectorBuilder}.
   *
   * @param s the {@link Server} for which the connector is being created
   * @param certificateValidator a {@link X509CertChainValidatorExt} used to validate certificates
   * @return an instance of the {@link TLSServerConnectorBuilder}
   */
  public static TLSServerConnectorBuilder instance(
      Server s, X509CertChainValidatorExt certificateValidator) {

    return new TLSServerConnectorBuilder(s, certificateValidator);
  }

  /**
   * Private ctor.
   *
   * @param s the {@link Server} for which the connector is being created
   * @param certificateValidator a {@link X509CertChainValidatorExt} used to validate certificates
   */
  private TLSServerConnectorBuilder(Server s, X509CertChainValidatorExt certificateValidator) {

    if (s == null) {
      throw new IllegalArgumentException("Server cannot be null");
    }

    if (certificateValidator == null) {
      throw new IllegalArgumentException("certificateValidator cannot be null");
    }

    this.server = s;
    this.certificateValidator = certificateValidator;
  }

  private void credentialsSanityChecks() {

    checkFileExistsAndIsReadable(new File(certificateFile), "Error accessing certificate file");

    checkFileExistsAndIsReadable(
        new File(certificateKeyFile), "Error accessing certificate key file");
  }

  private void loadCredentials() {

    credentialsSanityChecks();

    PEMCredential serviceCredentials = null;

    try {

      serviceCredentials =
          new PEMCredential(certificateKeyFile, certificateFile, certicateKeyPassword);

    } catch (KeyStoreException | CertificateException | IOException e) {

      throw new TLSConnectorBuilderError("Error setting up service credentials", e);
    }

    keyManager = serviceCredentials.getKeyManager();
  }

  /**
   * Configures SSL session parameters for the jetty {@link SslContextFactory}.
   *
   * @param contextFactory the {@link SslContextFactory} being configured
   */
  private void configureContextFactory(SslContextFactory.Server contextFactory) {

    if (excludeProtocols != null) {
      contextFactory.setExcludeProtocols(excludeProtocols);
    }

    if (includeProtocols != null) {
      contextFactory.setIncludeProtocols(includeProtocols);
    }

    if (excludeCipherSuites != null) {
      contextFactory.setExcludeCipherSuites(excludeCipherSuites);
    }

    if (includeCipherSuites != null) {
      contextFactory.setIncludeCipherSuites(includeCipherSuites);
    }

    contextFactory.setWantClientAuth(tlsWantClientAuth);
    contextFactory.setNeedClientAuth(tlsNeedClientAuth);

    if (useConscrypt) {
      contextFactory.setProvider(CONSCRYPT_PROVIDER);
    } else {
      contextFactory.setProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    if (hostnameVerifier != null) {
      contextFactory.setHostnameVerifier(hostnameVerifier);
    }

    if (disableJsseHostnameVerification) {
      contextFactory.setEndpointIdentificationAlgorithm(null);
    }
  }

  /**
   * Builds a default {@link HttpConfiguration} for the TLS-enabled connector being created
   *
   * @return the default {@link HttpConfiguration}
   */
  private HttpConfiguration defaultHttpConfiguration() {

    HttpConfiguration httpsConfig = new HttpConfiguration();

    httpsConfig.setSecureScheme("https");

    httpsConfig.setSecurePort(port);

    httpsConfig.setOutputBufferSize(32768);
    httpsConfig.setRequestHeaderSize(8192);
    httpsConfig.setResponseHeaderSize(8192);

    httpsConfig.setSendServerVersion(true);
    httpsConfig.setSendDateHeader(false);

    httpsConfig.addCustomizer(new SecureRequestCustomizer());

    return httpsConfig;
  }

  /**
   * Gives access to the {@link HttpConfiguration} used for the TLS-enabled connector being created.
   * If the configuration is not set, it creates it using {@link #defaultHttpConfiguration()}.
   *
   * @return the {@link HttpConfiguration} being used for the TLS-enabled connector.
   */
  public HttpConfiguration httpConfiguration() {

    if (httpConfiguration == null) {
      httpConfiguration = defaultHttpConfiguration();
    }

    return httpConfiguration;
  }

  /**
   * Sets the port for the connector being created.
   *
   * @param port the port for the connector
   * @return this builder
   */
  public TLSServerConnectorBuilder withPort(int port) {

    this.port = port;
    return this;
  }

  /**
   * Sets the certificate file for the connector being created.
   *
   * @param certificateFile the certificate file
   * @return this builder
   */
  public TLSServerConnectorBuilder withCertificateFile(String certificateFile) {

    this.certificateFile = certificateFile;
    return this;
  }

  /**
   * Sets the certificate key file for the connector being created.
   *
   * @param certificateKeyFile the certificate key file
   * @return this builder
   */
  public TLSServerConnectorBuilder withCertificateKeyFile(String certificateKeyFile) {

    this.certificateKeyFile = certificateKeyFile;
    return this;
  }

  /**
   * The the certificate key password for the connector being built
   *
   * @param certificateKeyPassword the certificate key password
   * @return this builder
   */
  public TLSServerConnectorBuilder withCertificateKeyPassword(char[] certificateKeyPassword) {

    this.certicateKeyPassword = certificateKeyPassword;
    return this;
  }

  /**
   * Sets the {@link SslContextFactory#setNeedClientAuth(boolean)} parameter for the connector being
   * created.
   *
   * @param needClientAuth true if client authentication is required
   * @return this builder
   */
  public TLSServerConnectorBuilder withNeedClientAuth(boolean needClientAuth) {

    this.tlsNeedClientAuth = needClientAuth;
    return this;
  }

  /**
   * Sets the {@link SslContextFactory#setWantClientAuth(boolean)} parameter for the connector being
   * created.
   *
   * @param wantClientAuth true if client authentication is wanted
   * @return this builder
   */
  public TLSServerConnectorBuilder withWantClientAuth(boolean wantClientAuth) {

    this.tlsWantClientAuth = wantClientAuth;
    return this;
  }

  /**
   * Sets SSL included protocols. See {@link SslContextFactory#setIncludeProtocols(String...)}.
   *
   * @param includeProtocols the array of included protocol names
   * @return this builder
   */
  public TLSServerConnectorBuilder withIncludeProtocols(String... includeProtocols) {

    this.includeProtocols = includeProtocols;
    return this;
  }

  /**
   * Sets SSL excluded protocols. See {@link SslContextFactory#setExcludeProtocols(String...)}.
   *
   * @param excludeProtocols the array of excluded protocol names
   * @return this builder
   */
  public TLSServerConnectorBuilder withExcludeProtocols(String... excludeProtocols) {

    this.excludeProtocols = excludeProtocols;
    return this;
  }

  /**
   * Sets the SSL included cipher suites.
   *
   * @param includeCipherSuites the array of included cipher suites.
   * @return this builder
   */
  public TLSServerConnectorBuilder withIncludeCipherSuites(String... includeCipherSuites) {

    this.includeCipherSuites = includeCipherSuites;
    return this;
  }

  /**
   * Sets the SSL ecluded cipher suites.
   *
   * @param excludeCipherSuites the array of excluded cipher suites.
   * @return this builder
   */
  public TLSServerConnectorBuilder withExcludeCipherSuites(String... excludeCipherSuites) {

    this.excludeCipherSuites = excludeCipherSuites;
    return this;
  }

  /**
   * Sets the {@link HttpConfiguration} for the connector being built.
   *
   * @param conf the {@link HttpConfiguration} to use
   * @return this builder
   */
  public TLSServerConnectorBuilder withHttpConfiguration(HttpConfiguration conf) {

    this.httpConfiguration = conf;
    return this;
  }

  /**
   * Sets the {@link KeyManager} for the connector being built.
   *
   * @param km the {@link KeyManager} to use
   * @return this builder
   */
  public TLSServerConnectorBuilder withKeyManager(KeyManager km) {

    this.keyManager = km;
    return this;
  }

  public TLSServerConnectorBuilder withConscrypt(boolean conscryptEnabled) {
    this.useConscrypt = conscryptEnabled;
    return this;
  }

  public TLSServerConnectorBuilder withHttp2(boolean http2Enabled) {
    this.enableHttp2 = http2Enabled;
    return this;
  }

  public TLSServerConnectorBuilder metricRegistry(MetricRegistry registry) {
    this.registry = registry;
    return this;
  }

  public TLSServerConnectorBuilder metricName(String metricName) {
    this.metricName = metricName;
    return this;
  }

  public TLSServerConnectorBuilder withTlsProtocol(String tlsProtocol) {
    this.tlsProtocol = tlsProtocol;
    return this;
  }

  public TLSServerConnectorBuilder withHostnameVerifier(HostnameVerifier verifier) {
    this.hostnameVerifier = verifier;
    return this;
  }

  public TLSServerConnectorBuilder withDisableJsseHostnameVerification(
      boolean disableJsseHostnameVerification) {
    this.disableJsseHostnameVerification = disableJsseHostnameVerification;
    return this;
  }

  public TLSServerConnectorBuilder withAcceptors(int acceptors) {
    this.acceptors = acceptors;
    return this;
  }

  public TLSServerConnectorBuilder withSelectors(int selectors) {
    this.selectors = selectors;
    return this;
  }

  private SSLContext buildSSLContext() {

    SSLContext sslCtx;

    try {

      KeyManager[] kms = new KeyManager[] {keyManager};
      SSLTrustManager tm = new SSLTrustManager(certificateValidator);

      if (useConscrypt) {

        if (Security.getProvider(CONSCRYPT_PROVIDER) == null) {
          Security.addProvider(new OpenSSLProvider());
        }

        sslCtx = SSLContext.getInstance(tlsProtocol, CONSCRYPT_PROVIDER);
      } else {
        sslCtx = SSLContext.getInstance(tlsProtocol);
      }

      sslCtx.init(kms, new TrustManager[] {tm}, null);

    } catch (NoSuchAlgorithmException e) {

      throw new TLSConnectorBuilderError("TLS protocol not supported: " + e.getMessage(), e);
    } catch (KeyManagementException e) {
      throw new TLSConnectorBuilderError(e);
    } catch (NoSuchProviderException e) {
      throw new TLSConnectorBuilderError("TLS provider error: " + e.getMessage(), e);
    }

    return sslCtx;
  }

  /**
   * Builds a {@link ServerConnector} based on the {@link TLSServerConnectorBuilder} parameters
   *
   * @return a {@link ServerConnector}
   */
  public ServerConnector build() {

    if (keyManager == null) {
      loadCredentials();
    }

    SSLContext sslContext = buildSSLContext();
    SslContextFactory.Server cf = new SslContextFactory.Server();

    cf.setSslContext(sslContext);

    configureContextFactory(cf);

    if (httpConfiguration == null) {
      httpConfiguration = defaultHttpConfiguration();
    }

    HttpConnectionFactory httpConnFactory = new HttpConnectionFactory(httpConfiguration);
    ConnectionFactory connFactory = null;

    if (registry != null) {
      connFactory = new InstrumentedConnectionFactory(httpConnFactory, registry.timer(metricName));
    } else {
      connFactory = httpConnFactory;
    }

    ConnectionFactory h2ConnFactory = null;
    ServerConnector connector = null;

    if (enableHttp2) {

      HTTP2ServerConnectionFactory h2cf = new HTTP2ServerConnectionFactory(httpConfiguration);

      if (registry != null) {
        h2ConnFactory = new InstrumentedConnectionFactory(h2cf, registry.timer(metricName));
      } else {
        h2ConnFactory = h2cf;
      }
      ALPNServerConnectionFactory alpn = createAlpnProtocolFactory(httpConnFactory);
      cf.setCipherComparator(HTTP2Cipher.COMPARATOR);
      cf.setUseCipherSuitesOrder(true);

      SslConnectionFactory sslCf = new SslConnectionFactory(cf, alpn.getProtocol());

      connector =
          new ServerConnector(
              server, acceptors, selectors, sslCf, alpn, h2ConnFactory, httpConnFactory);

    } else {

      connector =
          new ServerConnector(
              server,
              acceptors,
              selectors,
              new SslConnectionFactory(cf, HttpVersion.HTTP_1_1.asString()),
              connFactory);
    }

    connector.setPort(port);
    return connector;
  }

  private ALPNServerConnectionFactory createAlpnProtocolFactory(
      HttpConnectionFactory httpConnectionFactory) {
    ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
    alpn.setDefaultProtocol(httpConnectionFactory.getProtocol());
    return alpn;
  }

  /**
   * Checks that file exists and is readable.
   *
   * @param f the {@link File} to be checked
   * @param prefix A prefix string for the error message, in case the file does not exist and is not
   *     readable
   * @throws RuntimeException if the file does not exist or is not readable
   */
  private void checkFileExistsAndIsReadable(File f, String prefix) {

    String errorMessage = null;

    if (!f.exists()) {
      errorMessage = "File does not exists";
    } else if (!f.canRead()) {
      errorMessage = "File is not readable";
    } else if (f.isDirectory()) {
      errorMessage = "File is a directory";
    }

    if (errorMessage != null) {
      String msg = String.format("%s: %s [%s]", prefix, errorMessage, f.getAbsolutePath());
      throw new TLSConnectorBuilderError(msg);
    }
  }
}
