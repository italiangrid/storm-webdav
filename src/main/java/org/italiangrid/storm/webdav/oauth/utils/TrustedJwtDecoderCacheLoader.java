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

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.OAuthProperties.AuthorizationServer;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.oauth.UnknownTokenIssuerError;
import org.italiangrid.storm.webdav.oauth.validator.AudienceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoderJwkSupport;

import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

public class TrustedJwtDecoderCacheLoader extends CacheLoader<String, JwtDecoder> {

  public static final Logger LOG = LoggerFactory.getLogger(TrustedJwtDecoderCacheLoader.class);

  private final ServiceConfigurationProperties properties;

  private final OidcConfigurationFetcher fetcher;
  private final ExecutorService executor;
  private final OAuthProperties oauthProperties;

  @Autowired
  public TrustedJwtDecoderCacheLoader(ServiceConfigurationProperties properties,
      OAuthProperties oauthProperties, RestTemplateBuilder builder,
      OidcConfigurationFetcher fetcher, ExecutorService executor) {
    this.properties = properties;
    this.oauthProperties = oauthProperties;
    this.fetcher = fetcher;
    this.executor = executor;
  }

  public Supplier<UnknownTokenIssuerError> unknownTokenIssuer(String issuer) {
    return () -> new UnknownTokenIssuerError(issuer);
  }

  @Override
  public JwtDecoder load(String issuer) throws Exception {
    AuthorizationServer as = oauthProperties.getIssuers()
      .stream()
      .filter(i -> issuer.equals(i.getIssuer()))
      .findAny()
      .orElseThrow(unknownTokenIssuer(issuer));

    Map<String, Object> oidcConfiguration = fetcher.loadConfigurationForIssuer(issuer);

    NimbusJwtDecoderJwkSupport jwtDecoder =
        new NimbusJwtDecoderJwkSupport(oidcConfiguration.get("jwks_uri").toString());

    OAuth2TokenValidator<Jwt> jwtValidator = JwtValidators.createDefaultWithIssuer(issuer);

    if (as.isEnforceAudienceChecks()) {
      OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(as);
      jwtDecoder
        .setJwtValidator(new DelegatingOAuth2TokenValidator<>(jwtValidator, audienceValidator));
    } else {
      jwtDecoder.setJwtValidator(jwtValidator);
    }

    return jwtDecoder;
  }

  private boolean localTokenIssuer(String issuer) {
    return properties.getAuthzServer().isEnabled()
        && issuer.equals(properties.getAuthzServer().getIssuer());
  }

  @Override
  public ListenableFuture<JwtDecoder> reload(String issuer, JwtDecoder oldValue) throws Exception {
    
    LOG.debug("Scheduling reload configuration for OAuth issuer '{}'", issuer);
    
    if (localTokenIssuer(issuer)) {
      return Futures.immediateFuture(oldValue);
    }

    ListenableFutureTask<JwtDecoder> task = ListenableFutureTask.create(() -> load(issuer));
    executor.execute(task);

    return task;
  }
}
