// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.utils;

import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.OAuthProperties.AuthorizationServer;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.oauth.UnknownTokenIssuerError;
import org.italiangrid.storm.webdav.oauth.validator.AudienceValidator;
import org.italiangrid.storm.webdav.oauth.validator.WlcgProfileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.Cache;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class TrustedJwtDecoderCacheLoader extends CacheLoader<String, JwtDecoder> {

  public static final Logger LOG = LoggerFactory.getLogger(TrustedJwtDecoderCacheLoader.class);

  private final ServiceConfigurationProperties properties;

  private final OidcConfigurationFetcher fetcher;
  private final ExecutorService executor;
  private final OAuthProperties oauthProperties;

  public TrustedJwtDecoderCacheLoader(
      ServiceConfigurationProperties properties,
      OAuthProperties oauthProperties,
      RestTemplateBuilder builder,
      OidcConfigurationFetcher fetcher,
      ExecutorService executor) {
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
    AuthorizationServer as =
        oauthProperties.getIssuers().stream()
            .filter(i -> issuer.equals(i.getIssuer()))
            .findAny()
            .orElseThrow(unknownTokenIssuer(issuer));

    Map<String, Object> oidcConfiguration = fetcher.loadConfigurationForIssuer(issuer);
    URI jwksUri = URI.create(oidcConfiguration.get("jwks_uri").toString());
    Cache noExpirationCache = new NoExpirationStringCache(fetcher.loadJWKSourceForURL(jwksUri));

    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withJwkSetUri((oidcConfiguration.get("jwks_uri").toString()))
            .cache(noExpirationCache)
            .build();

    OAuth2TokenValidator<Jwt> jwtValidator = JwtValidators.createDefaultWithIssuer(issuer);
    OAuth2TokenValidator<Jwt> wlcgProfileValidator = new WlcgProfileValidator();

    List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
    validators.add(jwtValidator);
    validators.add(wlcgProfileValidator);

    if (as.isEnforceAudienceChecks()) {
      validators.add(new AudienceValidator(as));
    }

    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));

    return decoder;
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
