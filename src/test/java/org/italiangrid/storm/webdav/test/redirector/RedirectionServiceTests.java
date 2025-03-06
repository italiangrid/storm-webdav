// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.redirector;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Optional;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.ResourceAccessTokenRequest;
import org.italiangrid.storm.webdav.oauth.authzserver.ResourceAccessTokenRequest.Permission;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.SignedJwtTokenIssuer;
import org.italiangrid.storm.webdav.redirector.DefaultRedirectionService;
import org.italiangrid.storm.webdav.redirector.RedirectError;
import org.italiangrid.storm.webdav.redirector.ReplicaSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class RedirectionServiceTests extends RedirectorTestSupport {

  @Mock Authentication authentication;

  @Mock HttpServletRequest request;

  @Mock HttpServletResponse response;

  @Mock SignedJwtTokenIssuer tokenIssuer;

  @Mock SignedJWT token;

  @Mock ReplicaSelector selector;

  @Captor
  ArgumentCaptor<ResourceAccessTokenRequest> tokenRequest =
      ArgumentCaptor.forClass(ResourceAccessTokenRequest.class);

  ServiceConfigurationProperties config;
  DefaultRedirectionService service;

  @BeforeEach
  void setup() {
    config = buildConfigurationProperties();
    lenient()
        .when(tokenIssuer.createResourceAccessToken(Mockito.any(), Mockito.any()))
        .thenReturn(token);
    lenient().when(token.serialize()).thenReturn(RANDOM_TOKEN_STRING);
    lenient().when(selector.selectReplica()).thenReturn(Optional.empty());
    lenient().when(request.getServletPath()).thenReturn(PATH);
    lenient().when(request.getMethod()).thenReturn("GET");
    service = new DefaultRedirectionService(config, tokenIssuer, selector);
  }

  @Test
  void testRedirectFailureOnEmptyReplica() {

    Exception e =
        assertThrows(
            RedirectError.class,
            () -> {
              service.buildRedirect(authentication, request, response);
            });
    assertThat(e.getMessage(), containsString("No replica found"));
  }

  @Test
  void testRedirectUriConstruction() {
    when(selector.selectReplica()).thenReturn(Optional.of(REPLICA_0));

    String uriString = service.buildRedirect(authentication, request, response);
    verify(tokenIssuer).createResourceAccessToken(tokenRequest.capture(), Mockito.any());

    URI uri = URI.create(uriString);

    assertThat(uri.getHost(), is(URI_0_HOST));
    assertThat(uri.getScheme(), is(URI_0_SCHEME));
    assertThat(uri.getPath(), is(PATH));
    assertThat(uri.getQuery(), is(ACCESS_TOKEN_QUERY_STRING));
    assertThat(tokenRequest.getValue().getPath(), is(PATH));
    assertThat(tokenRequest.getValue().getPermission(), is(Permission.r));
    assertThat(
        tokenRequest.getValue().getLifetimeSecs(),
        is(config.getRedirector().getMaxTokenLifetimeSecs()));
  }

  @Test
  void testPutRedirectUriConstruction() {
    when(selector.selectReplica()).thenReturn(Optional.of(REPLICA_0));
    when(request.getMethod()).thenReturn("PUT");

    String uriString = service.buildRedirect(authentication, request, response);
    verify(tokenIssuer).createResourceAccessToken(tokenRequest.capture(), Mockito.any());

    URI uri = URI.create(uriString);

    assertThat(uri.getHost(), is(URI_0_HOST));
    assertThat(uri.getScheme(), is(URI_0_SCHEME));
    assertThat(uri.getPath(), is(PATH));
    assertThat(uri.getQuery(), is(ACCESS_TOKEN_QUERY_STRING));
    assertThat(tokenRequest.getValue().getPath(), is(PATH));
    assertThat(tokenRequest.getValue().getPermission(), is(Permission.rw));
    assertThat(
        tokenRequest.getValue().getLifetimeSecs(),
        is(config.getRedirector().getMaxTokenLifetimeSecs()));
  }

  @Test
  void testRedirectUriWithPrefixConstruction() {
    when(selector.selectReplica()).thenReturn(Optional.of(REPLICA_WITH_PREFIX));

    String uriString = service.buildRedirect(authentication, request, response);

    URI uri = URI.create(uriString);

    assertThat(uri.getHost(), is(URI_WITH_PREFIX_HOST));
    assertThat(uri.getScheme(), is(URI_WITH_PREFIX_SCHEME));
    assertThat(uri.getPath(), is(PATH_WITH_PREFIX));
    assertThat(uri.getQuery(), is(ACCESS_TOKEN_QUERY_STRING));
  }
}
