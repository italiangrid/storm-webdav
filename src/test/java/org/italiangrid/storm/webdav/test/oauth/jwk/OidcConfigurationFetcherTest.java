// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.oauth.jwk;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.oauth.utils.DefaultOidcConfigurationFetcher.ISSUER_MISMATCH_ERROR_TEMPLATE;
import static org.italiangrid.storm.webdav.oauth.utils.DefaultOidcConfigurationFetcher.NO_JWKS_URI_ERROR_TEMPLATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.HashMap;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class OidcConfigurationFetcherTest {

  static final String ISSUER = "https://iam-dev.cloud.cnaf.infn.it/";
  static final String JWK_URI = ISSUER + "jwk";

  static final String ANOTHER_ISSUER = "https://iam.cloud.infn.it/";
  static final String ANOTHER_JWK_URI = ANOTHER_ISSUER + "jwk";

  static final String KID = "rsa1";

  final ParameterizedTypeReference<Map<String, Object>> typeReference =
      new ParameterizedTypeReference<Map<String, Object>>() {};

  @Mock RestTemplate restTemplate;
  @Mock RestTemplateBuilder restBuilder;
  @Mock OAuthProperties oAuthProperties;

  private Map<String, Object> getMapWithIssuerAndJwkUri(String issuer, String jwkUri) {
    Map<String, Object> m = new HashMap<>();
    m.put("issuer", issuer);
    m.put("jwks_uri", jwkUri);
    return m;
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<Map<String, Object>> getWellKnownResponse(
      HttpStatus status, Map<String, Object> map) {

    ResponseEntity<Map<String, Object>> mockedEntity =
        (ResponseEntity<Map<String, Object>>) Mockito.mock(ResponseEntity.class);
    lenient().when(mockedEntity.getStatusCode()).thenReturn(HttpStatusCode.valueOf(status.value()));
    lenient().when(mockedEntity.getBody()).thenReturn(map);
    return mockedEntity;
  }

  private String loadJwkFromFile() throws IOException {

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("jwk/test-keystore.jwks").getFile());
    return FileUtils.readFileToString(file, "UTF-8");
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<String> getJWKURIResponse(HttpStatus status, String data) {

    ResponseEntity<String> mockedEntity =
        (ResponseEntity<String>) Mockito.mock(ResponseEntity.class);
    lenient().when(mockedEntity.getBody()).thenReturn(data);
    lenient().when(mockedEntity.getStatusCode()).thenReturn(HttpStatusCode.valueOf(status.value()));
    return mockedEntity;
  }

  private OidcConfigurationFetcher getFetcher(
      ResponseEntity<Map<String, Object>> wellKnownResponse, ResponseEntity<String> jwkResponse) {

    lenient().when(restTemplate.exchange(any(), eq(typeReference))).thenReturn(wellKnownResponse);
    lenient().when(restTemplate.exchange(any(), eq(String.class))).thenReturn(jwkResponse);
    return getFetcher(restTemplate);
  }

  private OidcConfigurationFetcher getFetcherWithException(
      ResponseEntity<Map<String, Object>> wellKnownResponse) {

    lenient().when(restTemplate.exchange(any(), eq(typeReference))).thenReturn(wellKnownResponse);
    lenient()
        .when(restTemplate.exchange(any(), eq(String.class)))
        .thenThrow(new RuntimeException("ERROR"));
    return getFetcher(restTemplate);
  }

  private OidcConfigurationFetcher getFetcher(RestTemplate restTemplate) {

    lenient().when(restBuilder.build()).thenReturn(restTemplate);
    lenient().when(restBuilder.connectTimeout(any())).thenReturn(restBuilder);
    lenient().when(restBuilder.readTimeout(any())).thenReturn(restBuilder);
    lenient().when(oAuthProperties.getRefreshTimeoutSeconds()).thenReturn(30);
    lenient().when(oAuthProperties.getRefreshPeriodMinutes()).thenReturn(1);
    return new DefaultOidcConfigurationFetcher(restBuilder, oAuthProperties);
  }

  private OidcConfigurationFetcher getSuccessfulFetcher() throws RestClientException, IOException {

    ResponseEntity<Map<String, Object>> mockedResponseMapEntity =
        getWellKnownResponse(OK, getMapWithIssuerAndJwkUri(ISSUER, JWK_URI));
    ResponseEntity<String> mockedResponseStringEntity = getJWKURIResponse(OK, loadJwkFromFile());
    return getFetcher(mockedResponseMapEntity, mockedResponseStringEntity);
  }

  private OidcConfigurationFetcher getSuccessfulFetcherWithWrongIssuer()
      throws RestClientException, IOException {

    ResponseEntity<Map<String, Object>> mockedResponseMapEntity =
        getWellKnownResponse(OK, getMapWithIssuerAndJwkUri(ANOTHER_ISSUER, ANOTHER_JWK_URI));
    ResponseEntity<String> mockedResponseStringEntity = getJWKURIResponse(OK, loadJwkFromFile());
    return getFetcher(mockedResponseMapEntity, mockedResponseStringEntity);
  }

  private OidcConfigurationFetcher getSuccessfulFetcherWithNoIssuer()
      throws RestClientException, IOException {

    Map<String, Object> map = getMapWithIssuerAndJwkUri(ANOTHER_ISSUER, ANOTHER_JWK_URI);
    map.remove("issuer");
    ResponseEntity<Map<String, Object>> mockedResponseMapEntity = getWellKnownResponse(OK, map);
    ResponseEntity<String> mockedResponseStringEntity = getJWKURIResponse(OK, loadJwkFromFile());
    return getFetcher(mockedResponseMapEntity, mockedResponseStringEntity);
  }

  private OidcConfigurationFetcher getSuccessfulFetcherWithNoJwk()
      throws RestClientException, IOException {

    Map<String, Object> map = getMapWithIssuerAndJwkUri(ISSUER, JWK_URI);
    map.remove("jwks_uri");
    ResponseEntity<Map<String, Object>> mockedResponseMapEntity = getWellKnownResponse(OK, map);
    ResponseEntity<String> mockedResponseStringEntity = getJWKURIResponse(OK, loadJwkFromFile());
    return getFetcher(mockedResponseMapEntity, mockedResponseStringEntity);
  }

  private OidcConfigurationFetcher getFetcherWithErrorOnFetch() throws RestClientException {

    ResponseEntity<Map<String, Object>> mockedResponseMapEntity =
        getWellKnownResponse(NOT_FOUND, null);
    return getFetcher(mockedResponseMapEntity, null);
  }

  private OidcConfigurationFetcher getFetcherWithErrorOnGetJwk() throws RestClientException {

    ResponseEntity<Map<String, Object>> mockedResponseMapEntity =
        getWellKnownResponse(OK, getMapWithIssuerAndJwkUri(ISSUER, JWK_URI));
    ResponseEntity<String> mockedResponseStringEntity = getJWKURIResponse(NOT_FOUND, null);
    return getFetcher(mockedResponseMapEntity, mockedResponseStringEntity);
  }

  private OidcConfigurationFetcher getFetcherWithRuntimeExceptionOnGetJwk()
      throws RestClientException {

    ResponseEntity<Map<String, Object>> mockedResponseMapEntity =
        getWellKnownResponse(OK, getMapWithIssuerAndJwkUri(ISSUER, JWK_URI));
    return getFetcherWithException(mockedResponseMapEntity);
  }

  @BeforeEach
  void setDebugLevel() {
    System.setProperty("logging.level.org.italiangrid.storm", "DEBUG");
  }

  @Test
  void fetchWellKnownEndpointWithSuccessTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getSuccessfulFetcher();
    Map<String, Object> conf = fetcher.loadConfigurationForIssuer(ISSUER);
    assertNotNull(conf);
    assertThat(conf.get("issuer"), is(ISSUER));
    assertThat(conf.get("jwks_uri"), is(JWK_URI));
  }

  @Test
  void fetchWellKnownEndpointWithErrorTests() throws RestClientException {

    OidcConfigurationFetcher fetcher = getFetcherWithErrorOnFetch();
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              fetcher.loadConfigurationForIssuer(ISSUER);
            });
    String expectedMessage = "Received status code: 404";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void fetchWellKnownEndpointWrongIssuerTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getSuccessfulFetcherWithWrongIssuer();
    OidcConfigurationResolutionError exception =
        assertThrows(
            OidcConfigurationResolutionError.class,
            () -> {
              fetcher.loadConfigurationForIssuer(ISSUER);
            });
    assertEquals(
        format(ISSUER_MISMATCH_ERROR_TEMPLATE, ANOTHER_ISSUER, ISSUER), exception.getMessage());
  }

  @Test
  void fetchWellKnownEndpointNoIssuerTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getSuccessfulFetcherWithNoIssuer();
    OidcConfigurationResolutionError exception =
        assertThrows(
            OidcConfigurationResolutionError.class,
            () -> {
              fetcher.loadConfigurationForIssuer(ISSUER);
            });
    assertEquals(
        format(ISSUER_MISMATCH_ERROR_TEMPLATE, "(unavailable)", ISSUER), exception.getMessage());
  }

  @Test
  void fetchWellKnownEndpointNoJwkTests() throws RestClientException, IOException {

    OidcConfigurationFetcher fetcher = getSuccessfulFetcherWithNoJwk();
    OidcConfigurationResolutionError exception =
        assertThrows(
            OidcConfigurationResolutionError.class,
            () -> {
              fetcher.loadConfigurationForIssuer(ISSUER);
            });
    assertEquals(format(NO_JWKS_URI_ERROR_TEMPLATE, ISSUER), exception.getMessage());
  }

  @Test
  void fetchJWKEndpointTests()
      throws RestClientException, IOException, ParseException, KeySourceException {

    OidcConfigurationFetcher fetcher = getSuccessfulFetcher();
    JWKSet key = JWKSet.parse(fetcher.loadJWKSourceForURL(URI.create(JWK_URI)));

    assertNotNull(key.getKeyByKeyId(KID));
    assertThat(key.getKeyByKeyId(KID).getKeyType(), is(KeyType.RSA));
  }

  @Test
  void fetchJWKEndpointWithErrorTests() throws RestClientException {

    OidcConfigurationFetcher fetcher = getFetcherWithErrorOnGetJwk();
    final URI jwkUri = URI.create(JWK_URI);
    KeySourceException exception =
        assertThrows(
            KeySourceException.class,
            () -> {
              fetcher.loadJWKSourceForURL(jwkUri);
            });
    String expectedMessage =
        "Unable to get JWK from '" + jwkUri + "': received status code " + NOT_FOUND.value();
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void fetchJWKEndpointWithRuntimeException() throws RestClientException {

    OidcConfigurationFetcher fetcher = getFetcherWithRuntimeExceptionOnGetJwk();
    final URI jwkUri = URI.create(JWK_URI);
    RemoteKeySourceException exception =
        assertThrows(
            RemoteKeySourceException.class,
            () -> {
              fetcher.loadJWKSourceForURL(jwkUri);
            });
    String expectedMessage = "Unable to get JWK from 'https://iam-dev.cloud.cnaf.infn.it/jwk'";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }
}
