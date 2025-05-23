// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oidc;

import static java.lang.String.format;

import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Provider;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.Builder;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.StringUtils;

public class ClientRegistrationCacheLoader extends CacheLoader<String, ClientRegistration> {

  public static final Logger LOG = LoggerFactory.getLogger(ClientRegistrationCacheLoader.class);

  public static final String ERROR_TEMPLATE = "Error initializing OIDC provider '%s': %s";
  final OAuth2ClientProperties clientProperties;
  final OAuthProperties oauthProperties;
  final ExecutorService executorService;

  public ClientRegistrationCacheLoader(
      OAuth2ClientProperties clientProperties,
      OAuthProperties oauthProperties,
      ExecutorService executorService) {
    this.clientProperties = clientProperties;
    this.oauthProperties = oauthProperties;
    this.executorService = executorService;
  }

  private ClientRegistration getClientRegistration(
      String registrationId,
      OAuth2ClientProperties.Registration properties,
      Map<String, Provider> providers) {
    String provider = properties.getProvider().strip();
    Builder builder = getBuilderFromIssuerIfPossible(registrationId, provider, providers);
    if (builder == null) {
      return null;
    }
    PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
    map.from(properties::getClientId).to(builder::clientId);
    map.from(properties::getClientSecret).to(builder::clientSecret);
    map.from(properties::getClientAuthenticationMethod)
        .as(ClientAuthenticationMethod::new)
        .to(builder::clientAuthenticationMethod);
    map.from(properties::getAuthorizationGrantType)
        .as(AuthorizationGrantType::new)
        .to(builder::authorizationGrantType);
    map.from(properties::getRedirectUri).to(builder::redirectUri);
    map.from(properties::getScope).as(StringUtils::toStringArray).to(builder::scope);
    map.from(properties::getClientName).to(builder::clientName);
    return builder.build();
  }

  private static Builder getBuilderFromIssuerIfPossible(
      String registrationId, String configuredProviderId, Map<String, Provider> providers) {
    String providerId = (configuredProviderId != null) ? configuredProviderId : registrationId;
    if (providers.containsKey(providerId)) {
      Provider provider = providers.get(providerId);
      String issuer = provider.getIssuerUri();
      if (issuer != null) {
        Builder builder =
            ClientRegistrations.fromOidcIssuerLocation(issuer).registrationId(registrationId);
        return getBuilder(builder, provider);
      }
    }
    return null;
  }

  private static Builder getBuilder(Builder builder, Provider provider) {
    PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
    map.from(provider::getAuthorizationUri).to(builder::authorizationUri);
    map.from(provider::getTokenUri).to(builder::tokenUri);
    map.from(provider::getUserInfoUri).to(builder::userInfoUri);
    map.from(provider::getUserInfoAuthenticationMethod)
        .as(AuthenticationMethod::new)
        .to(builder::userInfoAuthenticationMethod);
    map.from(provider::getJwkSetUri).to(builder::jwkSetUri);
    map.from(provider::getUserNameAttribute).to(builder::userNameAttributeName);
    return builder;
  }

  @Override
  public ClientRegistration load(String key) throws Exception {
    OAuth2ClientProperties.Registration reg = clientProperties.getRegistration().get(key);

    if (reg == null) {
      return null;
    }

    try {
      return getClientRegistration(key, reg, clientProperties.getProvider());
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new OidcProviderError(format(ERROR_TEMPLATE, reg.getClientName(), e.getMessage()), e);
    }
  }

  @Override
  public ListenableFuture<ClientRegistration> reload(String key, ClientRegistration oldValue)
      throws Exception {

    LOG.debug("Scheduling reload configuration for OpenID provider '{}'", key);
    ListenableFutureTask<ClientRegistration> task = ListenableFutureTask.create(() -> load(key));
    executorService.execute(task);
    return task;
  }
}
