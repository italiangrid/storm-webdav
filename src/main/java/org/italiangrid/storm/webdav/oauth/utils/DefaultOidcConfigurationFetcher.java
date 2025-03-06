// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.utils;

import static java.lang.String.format;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.RemoteKeySourceException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DefaultOidcConfigurationFetcher implements OidcConfigurationFetcher {

  public static final String WELL_KNOWN_FRAGMENT = "/.well-known/openid-configuration";
  public static final String ISSUER_MISMATCH_ERROR_TEMPLATE =
      "Issuer in metadata '%s' does not match with requested issuer '%s'";
  public static final String NO_JWKS_URI_ERROR_TEMPLATE =
      "No jwks_uri found in metadata for issuer '%s'";

  private static final MediaType APPLICATION_JWK_SET_JSON =
      new MediaType("application", "jwk-set+json");

  public static final Logger LOG = LoggerFactory.getLogger(DefaultOidcConfigurationFetcher.class);

  final RestTemplate restTemplate;

  public DefaultOidcConfigurationFetcher(
      RestTemplateBuilder restBuilder, OAuthProperties oAuthProperties) {
    final Duration timeout = Duration.ofSeconds(oAuthProperties.getRefreshTimeoutSeconds());
    this.restTemplate = restBuilder.connectTimeout(timeout).readTimeout(timeout).build();
  }

  private void metadataChecks(String issuer, Map<String, Object> oidcConfiguration) {
    String metadataIssuer = "(unavailable)";

    if (oidcConfiguration.containsKey("issuer")) {
      metadataIssuer = oidcConfiguration.get("issuer").toString();
    }

    if (!issuer.equals(metadataIssuer)) {
      throw new OidcConfigurationResolutionError(
          format(ISSUER_MISMATCH_ERROR_TEMPLATE, metadataIssuer, issuer));
    }

    if (!oidcConfiguration.containsKey("jwks_uri")) {
      throw new OidcConfigurationResolutionError(format(NO_JWKS_URI_ERROR_TEMPLATE, issuer));
    }
  }

  @Override
  public Map<String, Object> loadConfigurationForIssuer(String issuer) {
    LOG.debug("Fetching OpenID configuration for {}", issuer);

    ParameterizedTypeReference<Map<String, Object>> typeReference =
        new ParameterizedTypeReference<Map<String, Object>>() {};

    URI uri = UriComponentsBuilder.fromUriString(issuer + WELL_KNOWN_FRAGMENT).build().toUri();
    ResponseEntity<Map<String, Object>> response = null;
    try {
      response = restTemplate.exchange(RequestEntity.get(uri).build(), typeReference);
    } catch (RuntimeException e) {
      final String errorMsg = format("Unable to resolve OpenID configuration from '%s'", uri);
      if (LOG.isDebugEnabled()) {
        LOG.error("{}: {}", errorMsg, e.getMessage());
      }
      throw new OidcConfigurationResolutionError(errorMsg, e);
    }
    if (response.getStatusCode().value() != 200) {
      throw new OidcConfigurationResolutionError(
          format("Received status code: %s", response.getStatusCode().value()));
    }
    Map<String, Object> body = response.getBody();
    if (body == null) {
      throw new OidcConfigurationResolutionError("Received null body");
    }
    metadataChecks(issuer, body);
    return body;
  }

  @Override
  public String loadJWKSourceForURL(URI uri) throws KeySourceException {

    LOG.debug("Fetching JWK from {}", uri);

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, APPLICATION_JWK_SET_JSON));
    ResponseEntity<String> response = null;
    try {
      RequestEntity<Void> request = RequestEntity.get(uri).headers(headers).build();
      response = restTemplate.exchange(request, String.class);
    } catch (RuntimeException e) {
      final String errorMsg = format("Unable to get JWK from '%s'", uri);
      if (LOG.isDebugEnabled()) {
        LOG.error("{}: {}", errorMsg, e.getMessage());
      }
      throw new RemoteKeySourceException(errorMsg, e);
    }
    if (response.getStatusCode().value() != 200) {
      throw new KeySourceException(
          format(
              "Unable to get JWK from '%s': received status code %s",
              uri, response.getStatusCode().value()));
    }
    return response.getBody();
  }
}
