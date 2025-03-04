// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.authz.pdp;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;
import static org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer.ORIGIN_CLAIM;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import jakarta.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.pdp.LocalAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.Decision;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
public class LocalAuthzPdpTests {

  public static final String LOCAL_AUTHZ_SERVER_ISSUER = "https://issuer.example";
  public static final String REMOTE_ADDR = "192.168.1.1";
  public static final String ANOTHER_REMOTE_ADDR = "192.168.1.2";

  @Mock
  PathResolver pathResolver;

  @Mock
  LocalURLService localUrlService;

  @Mock
  Enumeration<String> requestHeaderNames;

  @Spy
  ServiceConfigurationProperties config = new ServiceConfigurationProperties();


  @Mock
  HttpServletRequest request;

  @Mock
  Jwt jwt;

  @Mock
  StorageAreaInfo sa;

  JwtAuthenticationToken jwtAuth;

  LocalAuthorizationPdp pdp;

  @BeforeEach
  void setup() throws MalformedURLException {

    AuthorizationServerProperties props = new AuthorizationServerProperties();
    props.setIssuer(LOCAL_AUTHZ_SERVER_ISSUER);
    config.setAuthzServer(props);

    jwtAuth = new JwtAuthenticationToken(jwt);

    lenient().when(jwt.getIssuer()).thenReturn(new URL(LOCAL_AUTHZ_SERVER_ISSUER));
    lenient().when(jwt.getClaimAsString("path")).thenReturn("/test/example");
    lenient().when(jwt.getClaimAsString("perms")).thenReturn("r");
    lenient().when(request.getServletPath()).thenReturn("/");
    lenient().when(request.getPathInfo()).thenReturn("test/example");
    lenient().when(request.getMethod()).thenReturn("GET");
    pdp = new LocalAuthorizationPdp(config);

  }

  @Test
  void noPathRaisesException() {

    when(jwt.getClaimAsString("path")).thenReturn(null);
    Exception e = assertThrows(IllegalArgumentException.class, () -> {
      pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    });
    assertThat(e.getMessage(), containsString("'path' claim not found"));
  }

  @Test
  void noPermsRaisesException() {

    when(jwt.getClaimAsString("perms")).thenReturn(null);
    Exception e = assertThrows(IllegalArgumentException.class, () -> {
      pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    });
    assertThat(e.getMessage(), containsString("'perms' claim not found"));
  }

  @Test
  void pathMismatchYeldsDeny() {
    when(jwt.getClaimAsString("path")).thenReturn("/test/another");

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
  }

  @Test
  void permMismatchYeldsDeny() {
    when(request.getMethod()).thenReturn("PUT");

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
  }

  @Test
  void originMismatchYeldsDeny() {
    when(request.getRemoteAddr()).thenReturn(REMOTE_ADDR);
    when(jwt.getClaimAsString(ORIGIN_CLAIM)).thenReturn(ANOTHER_REMOTE_ADDR);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
  }

  @Test
  void testPermit() {
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

}
