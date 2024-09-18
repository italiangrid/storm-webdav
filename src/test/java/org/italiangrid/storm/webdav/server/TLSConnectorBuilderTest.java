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
package org.italiangrid.storm.webdav.server;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.italiangrid.storm.webdav.server.util.CANLListener;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import eu.emi.security.authn.x509.CrlCheckingMode;
import eu.emi.security.authn.x509.NamespaceCheckingMode;
import eu.emi.security.authn.x509.OCSPCheckingMode;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;

@ExtendWith(MockitoExtension.class)
public class TLSConnectorBuilderTest {

  @Test
  void tlsConnectorBuilderErrorTests() {

    TLSConnectorBuilderError e = new TLSConnectorBuilderError("This is an error!");
    assertThat(e.getMessage(), is("This is an error!"));
    e = new TLSConnectorBuilderError("This is an error!", new RuntimeException());
    assertThat(e.getMessage(), is("This is an error!"));
    e = new TLSConnectorBuilderError(new RuntimeException("This is an error!"));
    assertThat(e.getCause() instanceof RuntimeException, is(true));
    assertThat(e.getMessage(), containsString("This is an error!"));
  }

  @Test
  void illegalArgumentExceptionThrown() {

    Server server = Mockito.mock(Server.class);
    X509CertChainValidatorExt validator = Mockito.mock(X509CertChainValidatorExt.class);

    assertThrows(IllegalArgumentException.class, () -> {
      TLSServerConnectorBuilder.instance(null, validator);
    });
    assertThrows(IllegalArgumentException.class, () -> {
      TLSServerConnectorBuilder.instance(server, null);
    });
    assertThrows(IllegalArgumentException.class, () -> {
      TLSServerConnectorBuilder.instance(null, null);
    });
  }

  private X509CertChainValidatorExt getValidator() {

    CANLListener l = new org.italiangrid.storm.webdav.server.util.CANLListener();
    CertificateValidatorBuilder builder = new CertificateValidatorBuilder();

    long refreshInterval = TimeUnit.SECONDS.toMillis(3600);

    return builder.namespaceChecks(NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS_REQUIRE)
      .crlChecks(CrlCheckingMode.IF_VALID)
      .ocspChecks(OCSPCheckingMode.IGNORE)
      .lazyAnchorsLoading(false)
      .storeUpdateListener(l)
      .validationErrorListener(l)
      .trustAnchorsDir("src/test/resources/trust-anchors")
      .trustAnchorsUpdateInterval(refreshInterval)
      .build();
  }

  @Test
  void tlsConnectorBuilderTests() {

    Server server = new Server();
    X509CertChainValidatorExt validator = getValidator();
    TLSServerConnectorBuilder builder = TLSServerConnectorBuilder.instance(server, validator);
    HttpConfiguration httpConfiguration = builder.httpConfiguration();
    KeyManager keyManager = Mockito.mock(KeyManager.class);
    builder.withPort(1234)
      .withCertificateFile("fake-certificate")
      .withCertificateKeyFile("fake-key")
      .withCertificateKeyPassword("secret".toCharArray())
      .withHttpConfiguration(httpConfiguration)
      .withKeyManager(keyManager)
      .withExcludeCipherSuites("one", "two")
      .withIncludeCipherSuites("three", "four")
      .withIncludeProtocols("protocol", "another-protocol")
      .withExcludeProtocols("another-more-protocol")
      .withHostnameVerifier(new NoopHostnameVerifier())
      .withConscrypt(false);

    ServerConnector c = builder.build();
    assertThat(c.getPort(), is(1234));
  }
}