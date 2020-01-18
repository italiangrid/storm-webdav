/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DefaultOidcConfigurationFetcher implements OidcConfigurationFetcher {

  public static final String WELL_KNOWN_FRAGMENT = "/.well-known/openid-configuration";
  public static final String ISSUER_MISMATCH_ERROR_TEMPLATE =
      "Isser in medatadata '%s' does not match with requested issuer '%s'";
  public static final String NO_JWKS_URI_ERROR_TEMPLATE = 
      "No jwks_uri found in metadata for issuer '%s'";

  public static final Logger LOG = LoggerFactory.getLogger(DefaultOidcConfigurationFetcher.class);

  final RestTemplateBuilder restBuilder;

  @Autowired
  public DefaultOidcConfigurationFetcher(RestTemplateBuilder restBuilder) {
    this.restBuilder = restBuilder;
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

}
