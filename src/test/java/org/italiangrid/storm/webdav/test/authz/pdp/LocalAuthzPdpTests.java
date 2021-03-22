/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.test.authz.pdp;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;
import static org.italiangrid.storm.webdav.oauth.authzserver.jwt.DefaultJwtTokenIssuer.ORIGIN_CLAIM;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.pdp.LocalAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.Decision;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.AuthorizationServerProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@RunWith(MockitoJUnitRunner.class)
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

  @Before
  public void setup() throws MalformedURLException {

    AuthorizationServerProperties props = new AuthorizationServerProperties();
    props.setIssuer(LOCAL_AUTHZ_SERVER_ISSUER);
    config.setAuthzServer(props);

    jwtAuth = new JwtAuthenticationToken(jwt);

    when(jwt.getIssuer()).thenReturn(new URL(LOCAL_AUTHZ_SERVER_ISSUER));
    when(jwt.getClaimAsString("path")).thenReturn("/test/example");
    when(jwt.getClaimAsString("perms")).thenReturn("r");
    when(request.getServletPath()).thenReturn("/");
    when(request.getPathInfo()).thenReturn("test/example");
    when(request.getMethod()).thenReturn("GET");
    pdp = new LocalAuthorizationPdp(config);

  }

  @Test(expected = IllegalArgumentException.class)
  public void noPathRaisesException() throws Exception {

    when(jwt.getClaimAsString("path")).thenReturn(null);
    try {
      pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), containsString("'path' claim not found"));
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void noPermsRaisesException() throws Exception {

    when(jwt.getClaimAsString("perms")).thenReturn(null);
    try {
      pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), containsString("'perms' claim not found"));
      throw e;
    }
  }

  @Test
  public void pathMismatchYeldsDeny() throws Exception {
    when(jwt.getClaimAsString("path")).thenReturn("/test/another");

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
  }

  @Test
  public void permMismatchYeldsDeny() throws Exception {
    when(request.getMethod()).thenReturn("PUT");

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
  }

  @Test
  public void originMismatchYeldsDeny() throws Exception {
    when(request.getRemoteAddr()).thenReturn(REMOTE_ADDR);
    when(jwt.getClaimAsString(ORIGIN_CLAIM)).thenReturn(ANOTHER_REMOTE_ADDR);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
  }

  @Test
  public void testPermit() throws Exception {
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

}
