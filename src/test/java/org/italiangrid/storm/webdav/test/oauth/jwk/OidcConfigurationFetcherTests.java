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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.oauth.utils.DefaultOidcConfigurationFetcher;
import org.italiangrid.storm.webdav.oauth.utils.OidcConfigurationFetcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Maps;
import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;

@ExtendWith(MockitoExtension.class)
public class OidcConfigurationFetcherTests {

  final static String ISSUER = "https://iam-dev.cloud.cnaf.infn.it/";
  final static String JWK_URI = "https://iam-dev.cloud.cnaf.infn.it/jwk";

  final static String KID = "rsa1";
  

  final ParameterizedTypeReference<Map<String, Object>> typeReference =
      new ParameterizedTypeReference<Map<String, Object>>() {};

  @Mock
  RestTemplate restTemplate;
  @Mock
  RestTemplateBuilder restBuilder;
  @Mock
  OAuthProperties oAuthProperties;

  @SuppressWarnings("unchecked")
  private ResponseEntity<Map<String, Object>> getMockedResponseFromWellKnown() {

    Map<String, Object> wellKnownMap = Maps.newHashMap();
    wellKnownMap.put("issuer", ISSUER);
    wellKnownMap.put("jwks_uri", JWK_URI);
    ResponseEntity<Map<String, Object>> mockedEntity = (ResponseEntity<Map<String, Object>>) Mockito.mock(ResponseEntity.class);
    lenient().when(mockedEntity.getBody()).thenReturn(wellKnownMap);
    return mockedEntity;
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<String> getMockedResponseFromJWKURI() throws IOException {

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("jwk/test-keystore.jwks").getFile());
    String data = FileUtils.readFileToString(file, "UTF-8");
    ResponseEntity<String> mockedEntity = (ResponseEntity<String>) Mockito.mock(ResponseEntity.class);
    lenient().when(mockedEntity.getBody()).thenReturn(data);
    lenient().when(mockedEntity.getStatusCodeValue()).thenReturn(200);
    return mockedEntity;
  }
  
  private OidcConfigurationFetcher getFetcher() throws RestClientException, IOException {

    ResponseEntity<Map<String, Object>> mockedResponseMapEntity = getMockedResponseFromWellKnown();
    lenient().when(restTemplate.exchange(any(), eq(typeReference))).thenReturn(mockedResponseMapEntity);
    ResponseEntity<String> mockedResponseStringEntity = getMockedResponseFromJWKURI();
    lenient().when(restTemplate.exchange(any(), eq(String.class))).thenReturn(mockedResponseStringEntity);

    lenient().when(restBuilder.build()).thenReturn(restTemplate);
    lenient().when(restBuilder.setConnectTimeout(any())).thenReturn(restBuilder);
    lenient().when(restBuilder.setReadTimeout(any())).thenReturn(restBuilder);
    lenient().when(oAuthProperties.getRefreshTimeoutSeconds()).thenReturn(30);
    lenient().when(oAuthProperties.getRefreshPeriodMinutes()).thenReturn(1);

    return new DefaultOidcConfigurationFetcher(restBuilder, oAuthProperties);
  }

  @Test
  public void fetchWellKnownEndpointTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getFetcher();
    Map<String, Object> conf = fetcher.loadConfigurationForIssuer(ISSUER);
    assertNotNull(conf);
    assertThat(conf.get("issuer"), is(ISSUER));
    assertThat(conf.get("jwks_uri"), is(JWK_URI));
  }

  @Test
  public void fetchJWKEndpointTests() throws RestClientException, IOException, RemoteKeySourceException, ParseException {

    OidcConfigurationFetcher fetcher = getFetcher();
    JWKSet key = JWKSet.parse(fetcher.loadJWKSourceForURL(URI.create(JWK_URI)));

    assertNotNull(key.getKeyByKeyId(KID));
    assertThat(key.getKeyByKeyId(KID).getKeyType(), is(KeyType.RSA));
  }
}
