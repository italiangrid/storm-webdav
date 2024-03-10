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
package org.italiangrid.storm.webdav.oauth.utils;

import static java.lang.String.format;

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

import com.nimbusds.jose.RemoteKeySourceException;

@Service
public class DefaultOidcConfigurationFetcher implements OidcConfigurationFetcher {

  public static final String WELL_KNOWN_FRAGMENT = "/.well-known/openid-configuration";
  public static final String ISSUER_MISMATCH_ERROR_TEMPLATE =
      "Issuer in metadata '%s' does not match with requested issuer '%s'";
  public static final String NO_JWKS_URI_ERROR_TEMPLATE = 
      "No jwks_uri found in metadata for issuer '%s'";

  private static final MediaType APPLICATION_JWK_SET_JSON = new MediaType("application", "jwk-set+json");

  public static final Logger LOG = LoggerFactory.getLogger(DefaultOidcConfigurationFetcher.class);

  final RestTemplateBuilder restBuilder;
  final OAuthProperties oAuthProperties;

  public DefaultOidcConfigurationFetcher(RestTemplateBuilder restBuilder,
      OAuthProperties oAuthProperties) {
    this.restBuilder = restBuilder;
    this.oAuthProperties = oAuthProperties;
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
      throw new OidcConfigurationResolutionError(format(NO_JWKS_URI_ERROR_TEMPLATE,issuer));
    }
  }

  @Override
  public Map<String, Object> loadConfigurationForIssuer(String issuer) {
    LOG.debug("Fetching OpenID configuration for {}", issuer);
    
    ParameterizedTypeReference<Map<String, Object>> typeReference =
        new ParameterizedTypeReference<Map<String, Object>>() {};

    RestTemplate rest = restBuilder.build();

    URI uri = UriComponentsBuilder.fromUriString(issuer + WELL_KNOWN_FRAGMENT).build().toUri();

    try {

      RequestEntity<Void> request = RequestEntity.get(uri).build();
      Map<String, Object> conf = rest.exchange(request, typeReference).getBody();
      metadataChecks(issuer, conf);
      return conf; 
    } catch (RuntimeException e) {
      final String errorMsg =
          format("Unable to resolve OpenID configuration for issuer '%s' from '%s': %s", issuer,
              uri, e.getMessage());

      if (LOG.isDebugEnabled()) {
        LOG.error(errorMsg, e);
      }

      throw new OidcConfigurationResolutionError(errorMsg, e);
    }
  }

  @Override
  public String loadJWKSourceForURL(URI uri) throws RemoteKeySourceException {

    LOG.debug("Fetching JWK from {}", uri);

    final Duration TIMEOUT = Duration.ofSeconds(oAuthProperties.getRefreshTimeoutSeconds());
    RestTemplate rest = restBuilder.setConnectTimeout(TIMEOUT).setReadTimeout(TIMEOUT).build();

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, APPLICATION_JWK_SET_JSON));
    ResponseEntity<String> response = null;
    try {
      RequestEntity<Void> request = RequestEntity.get(uri).headers(headers).build();
      response = rest.exchange(request, String.class);
      if (response.getStatusCodeValue() != 200) {
        throw new RuntimeException(format("Received status code: %s", response.getStatusCodeValue()));
      }
    } catch (RuntimeException e) {
      final String errorMsg = format("Unable to get JWK from '%s'", uri);
      if (LOG.isDebugEnabled()) {
        LOG.error("{}: {}", errorMsg, e.getMessage());
      }
      throw new RemoteKeySourceException(errorMsg, e);
    }

    return response.getBody();
  }

}
