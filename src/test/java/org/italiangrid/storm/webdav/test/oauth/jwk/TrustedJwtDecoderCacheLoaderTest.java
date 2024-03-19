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
package org.italiangrid.storm.webdav.test.oauth.jwk;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.config.OAuthProperties.AuthorizationServer;
import org.italiangrid.storm.webdav.oauth.utils.OidcConfigurationFetcher;
import org.italiangrid.storm.webdav.oauth.utils.TrustedJwtDecoderCacheLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.nimbusds.jose.RemoteKeySourceException;

@ExtendWith(MockitoExtension.class)
public class TrustedJwtDecoderCacheLoaderTest {

  private final String ISSUER = "https://wlcg.cloud.cnaf.infn.it/";
  private final String JWK_URI = "https://wlcg.cloud.cnaf.infn.it/jwks";

  @Mock
  ServiceConfigurationProperties properties;
  @Mock
  OAuthProperties oauthProperties;
  @Mock
  RestTemplateBuilder builder;
  @Mock
  OidcConfigurationFetcher fetcher;

  private ExecutorService executor;
  private TrustedJwtDecoderCacheLoader jwtLoader;

  @BeforeEach
  public void setup() throws IOException, RemoteKeySourceException {

    AuthorizationServer as = new AuthorizationServer();
    as.setIssuer(ISSUER);
    as.setJwkUri(JWK_URI);
    List<AuthorizationServer> issuerServers = Lists.newArrayList(as);
    lenient().when(oauthProperties.getIssuers()).thenReturn(issuerServers);

    Map<String, Object> oidcConfiguration = Maps.newHashMap();
    oidcConfiguration.put("issuer", ISSUER);
    oidcConfiguration.put("jwks_uri", JWK_URI);

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("jwk/test-keystore.jwks").getFile());
    String data = FileUtils.readFileToString(file, "UTF-8");

    lenient().when(fetcher.loadConfigurationForIssuer(ISSUER)).thenReturn(oidcConfiguration);
    lenient().when(fetcher.loadJWKSourceForURL(URI.create(JWK_URI))).thenReturn(data);

    AuthorizationServerProperties props = new AuthorizationServerProperties();
    props.setEnabled(false);
    props.setIssuer("http://localhost");
    lenient().when(properties.getAuthzServer()).thenReturn(props);

    executor = Executors.newScheduledThreadPool(1);

    jwtLoader =
        new TrustedJwtDecoderCacheLoader(properties, oauthProperties, builder, fetcher, executor);

  }

  @Test
  public void testLoadRemoteIssuerConfiguration() throws Exception {

    JwtDecoder decoder = jwtLoader.load(ISSUER);
    assertTrue(decoder instanceof NimbusJwtDecoder);
    ListenableFuture<JwtDecoder> reloaded = jwtLoader.reload(ISSUER, decoder);
    JwtDecoder newDecoder = reloaded.get();
    assertTrue(newDecoder instanceof NimbusJwtDecoder);
  }
}
