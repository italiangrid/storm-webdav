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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.oauth.utils.DefaultOidcConfigurationFetcher;
import org.italiangrid.storm.webdav.oauth.utils.OidcConfigurationFetcher;
import org.italiangrid.storm.webdav.oauth.utils.OidcConfigurationResolutionError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
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
  final static String JWK_URI = ISSUER + "jwk";

  final static String ANOTHER_ISSUER = "https://iam.cloud.infn.it/";
  final static String ANOTHER_JWK_URI = ANOTHER_ISSUER + "jwk";

  final static String KID = "rsa1";
  

  final ParameterizedTypeReference<Map<String, Object>> typeReference =
      new ParameterizedTypeReference<Map<String, Object>>() {};

  @Mock
  RestTemplate restTemplate;
  @Mock
  RestTemplateBuilder restBuilder;
  @Mock
  OAuthProperties oAuthProperties;

  private Map<String, Object> getMapWithIssuerAndJwkUri(String issuer, String jwkUri) {
    Map<String, Object> m = Maps.newHashMap();
    m.put("issuer", issuer);
    m.put("jwks_uri", jwkUri);
    return m;
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<Map<String, Object>> getWellKnownResponse(HttpStatus status, Map<String, Object> map) {

    ResponseEntity<Map<String, Object>> mockedEntity = (ResponseEntity<Map<String, Object>>) Mockito.mock(ResponseEntity.class);
    lenient().when(mockedEntity.getStatusCode()).thenReturn(status);
    lenient().when(mockedEntity.getStatusCodeValue()).thenReturn(status.value());
    lenient().when(mockedEntity.getBody()).thenReturn(map);
    return mockedEntity;
  }

  private String loadJwkFromFile() throws IOException {

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("jwk/test-keystore.jwks").getFile());
    return FileUtils.readFileToString(file, "UTF-8");
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<String> getJWKURIResponse(HttpStatus status, String data) throws IOException {

    ResponseEntity<String> mockedEntity = (ResponseEntity<String>) Mockito.mock(ResponseEntity.class);
    lenient().when(mockedEntity.getBody()).thenReturn(data);
    lenient().when(mockedEntity.getStatusCode()).thenReturn(status);
    lenient().when(mockedEntity.getStatusCodeValue()).thenReturn(status.value());
    return mockedEntity;
  }

  private OidcConfigurationFetcher getFetcher(ResponseEntity<Map<String, Object>> wellKnownResponse, ResponseEntity<String> jwkResponse) {

    lenient().when(restTemplate.exchange(any(), eq(typeReference))).thenReturn(wellKnownResponse);
    lenient().when(restTemplate.exchange(any(), eq(String.class))).thenReturn(jwkResponse);
    lenient().when(restBuilder.build()).thenReturn(restTemplate);
    lenient().when(restBuilder.setConnectTimeout(any())).thenReturn(restBuilder);
    lenient().when(restBuilder.setReadTimeout(any())).thenReturn(restBuilder);
    lenient().when(oAuthProperties.getRefreshTimeoutSeconds()).thenReturn(30);
    lenient().when(oAuthProperties.getRefreshPeriodMinutes()).thenReturn(1);
    return new DefaultOidcConfigurationFetcher(restBuilder, oAuthProperties);
  }

  private OidcConfigurationFetcher getSuccessfulFetcher() throws RestClientException, IOException {

    ResponseEntity<Map<String, Object>> mockedResponseMapEntity = getWellKnownResponse(OK, getMapWithIssuerAndJwkUri(ISSUER, JWK_URI));
    ResponseEntity<String> mockedResponseStringEntity = getJWKURIResponse(OK, loadJwkFromFile());
    return getFetcher(mockedResponseMapEntity, mockedResponseStringEntity);
  }

  private OidcConfigurationFetcher getSuccessfulFetcherWithWrongIssuer() throws RestClientException, IOException {

    ResponseEntity<Map<String, Object>> mockedResponseMapEntity = getWellKnownResponse(OK, getMapWithIssuerAndJwkUri(ANOTHER_ISSUER, ANOTHER_JWK_URI));
    ResponseEntity<String> mockedResponseStringEntity = getJWKURIResponse(OK, loadJwkFromFile());
    return getFetcher(mockedResponseMapEntity, mockedResponseStringEntity);
  }

  private OidcConfigurationFetcher getSuccessfulFetcherWithNoIssuer() throws RestClientException, IOException {

    Map<String, Object> map = getMapWithIssuerAndJwkUri(ANOTHER_ISSUER, ANOTHER_JWK_URI);
    map.remove("issuer");
    ResponseEntity<Map<String, Object>> mockedResponseMapEntity = getWellKnownResponse(OK, map);
    ResponseEntity<String> mockedResponseStringEntity = getJWKURIResponse(OK, loadJwkFromFile());
    return getFetcher(mockedResponseMapEntity, mockedResponseStringEntity);
  }

  private OidcConfigurationFetcher getSuccessfulFetcherWithNoJwk() throws RestClientException, IOException {

    Map<String, Object> map = getMapWithIssuerAndJwkUri(ISSUER, JWK_URI);
    map.remove("jwks_uri");
    ResponseEntity<Map<String, Object>> mockedResponseMapEntity = getWellKnownResponse(OK, map);
    ResponseEntity<String> mockedResponseStringEntity = getJWKURIResponse(OK, loadJwkFromFile());
    return getFetcher(mockedResponseMapEntity, mockedResponseStringEntity);
  }

  private OidcConfigurationFetcher getFetcherWithErrorOnFetch() throws RestClientException, IOException {

    ResponseEntity<Map<String, Object>> mockedResponseMapEntity = getWellKnownResponse(NOT_FOUND, null);
    return getFetcher(mockedResponseMapEntity, null);
  }

  private OidcConfigurationFetcher getFetcherWithErrorOnGetJwk() throws RestClientException, IOException {

    ResponseEntity<Map<String, Object>> mockedResponseMapEntity = getWellKnownResponse(OK, getMapWithIssuerAndJwkUri(ISSUER, JWK_URI));
    ResponseEntity<String> mockedResponseStringEntity = getJWKURIResponse(NOT_FOUND, null);
    return getFetcher(mockedResponseMapEntity, mockedResponseStringEntity);
  }

  @BeforeEach
  public void setDebugLevel() {
    System.setProperty("logging.level.org.italiangrid.storm", "DEBUG");
  }

  @Test
  public void fetchWellKnownEndpointWithSuccessTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getSuccessfulFetcher();
    Map<String, Object> conf = fetcher.loadConfigurationForIssuer(ISSUER);
    assertNotNull(conf);
    assertThat(conf.get("issuer"), is(ISSUER));
    assertThat(conf.get("jwks_uri"), is(JWK_URI));
  }

  @Test
  public void fetchWellKnownEndpointWithErrorTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getFetcherWithErrorOnFetch();
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      fetcher.loadConfigurationForIssuer(ISSUER);
    });
    String expectedMessage = "Unable to resolve OpenID configuration from '" + ISSUER + ".well-known/openid-configuration'";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  public void fetchWellKnownEndpointWrongIssuerTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getSuccessfulFetcherWithWrongIssuer();
    OidcConfigurationResolutionError exception = assertThrows(OidcConfigurationResolutionError.class, () -> {
      fetcher.loadConfigurationForIssuer(ISSUER);
    });
    String expectedMessage = "Unable to resolve OpenID configuration from '" + ISSUER + ".well-known/openid-configuration'";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
    
  }

  @Test
  public void fetchWellKnownEndpointNoIssuerTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getSuccessfulFetcherWithNoIssuer();
    OidcConfigurationResolutionError exception = assertThrows(OidcConfigurationResolutionError.class, () -> {
      fetcher.loadConfigurationForIssuer(ISSUER);
    });
    String expectedMessage = "Unable to resolve OpenID configuration from '" + ISSUER + ".well-known/openid-configuration'";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  public void fetchWellKnownEndpointNoJwkTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getSuccessfulFetcherWithNoJwk();
    OidcConfigurationResolutionError exception = assertThrows(OidcConfigurationResolutionError.class, () -> {
      fetcher.loadConfigurationForIssuer(ISSUER);
    });
    String expectedMessage = "Unable to resolve OpenID configuration from '" + ISSUER + ".well-known/openid-configuration'";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  public void fetchJWKEndpointTests() throws RestClientException, IOException, RemoteKeySourceException, ParseException {

    OidcConfigurationFetcher fetcher = getSuccessfulFetcher();
    JWKSet key = JWKSet.parse(fetcher.loadJWKSourceForURL(URI.create(JWK_URI)));

    assertNotNull(key.getKeyByKeyId(KID));
    assertThat(key.getKeyByKeyId(KID).getKeyType(), is(KeyType.RSA));
  }

  @Test
  public void fetcJWKEndpointWithErrorTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getFetcherWithErrorOnGetJwk();
    final URI jwkUri = URI.create(JWK_URI);
    RemoteKeySourceException exception = assertThrows(RemoteKeySourceException.class, () -> {
      fetcher.loadJWKSourceForURL(jwkUri);
    });
    String expectedMessage = "Unable to get JWK from '" + jwkUri + "'";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }
}
