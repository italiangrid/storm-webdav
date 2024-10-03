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
package org.italiangrid.storm.webdav.test.authz.pdp;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;
import static org.italiangrid.storm.webdav.authz.pdp.WlcgStructuredPathAuthorizationPdp.SCOPE_CLAIM;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.Decision;
import org.italiangrid.storm.webdav.authz.pdp.WlcgStructuredPathAuthorizationPdp;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@ExtendWith(MockitoExtension.class)
public class ScopePathAuthzPdpTests {

  public static final String[] CATCHALL_METHODS = {"HEAD", "OPTIONS"};
  public static final String[] READ_METHODS = {"GET", "PROPFIND"};
  public static final String[] REPLACE_METHODS = {"PUT", "MKCOL"};
  public static final String[] MODIFY_METHODS = {"DELETE", "PATCH"};
  public static final String COPY_METHOD = "COPY";
  public static final String MOVE_METHOD = "MOVE";

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

  @InjectMocks
  WlcgStructuredPathAuthorizationPdp pdp;

  @BeforeEach
  public void setup() throws MalformedURLException {
    jwtAuth = new JwtAuthenticationToken(jwt);
    lenient().when(jwt.getIssuer()).thenReturn(new URL("https://issuer.example"));
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("storage.read:/");
    lenient().when(request.getServletPath()).thenReturn("/");
    lenient().when(request.getPathInfo()).thenReturn("test/example");
    lenient().when(sa.accessPoints()).thenReturn(Lists.newArrayList("/test"));
    lenient().when(sa.orgs()).thenReturn(Sets.newHashSet("https://issuer.example"));
    lenient().when(pathResolver.resolveStorageArea("/test/example")).thenReturn(sa);
  }

  @Test
  void invalidAuthentication() {

    Authentication auth = mock(Authentication.class);
    assertThrows(IllegalArgumentException.class, () -> {
      pdp.authorizeRequest(newAuthorizationRequest(request, auth));
    });
  }

  @Test
  void noScopeClaimYeldsIndeterminate() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn(null);
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.INDETERMINATE));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("no scope"));
  }

  @Test
  void noSaYeldsIndeterminate() {

    when(pathResolver.resolveStorageArea("/test/example")).thenReturn(null);
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.INDETERMINATE));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("No storage area"));
  }

  @Test
  void noStorageScopesYeldsDeny() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid profile storage.read");

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.INDETERMINATE));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
  }

  @Test
  void noStorageScopesYeldsDenyForCatchallMethods() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid profile");

    for (String m : CATCHALL_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

      assertThat(result.getDecision(), is(Decision.INDETERMINATE));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }
  }

  @Test
  void catchallMethodsRequestsAtLeastOneStorageScope() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : CATCHALL_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : CATCHALL_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.create:/");

    for (String m : CATCHALL_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.stage:/");

    for (String m : CATCHALL_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  void readMethodsRequestsRequireStorageReadOrStage() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : READ_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : READ_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.stage:/");

    for (String m : READ_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  void replaceMethodsRequestsRequireStorageModifyOrCreate() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.create:/");
    when(pathResolver.pathExists("/test/example")).thenReturn(true);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    when(pathResolver.pathExists("/test/example")).thenReturn(false);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  void modifyMethodsRequestsRequireStorageModifyOrCreate() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.create:/");

    for (String m : MODIFY_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.modify:/");

    for (String m : MODIFY_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  void testLocalCopyRequiresStorageCreateOrModify() {

    when(request.getMethod()).thenReturn(COPY_METHOD);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.write:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

  @Test
  void testPullTpcRequiresCreateOrModify() {
    when(request.getMethod()).thenReturn(COPY_METHOD);
    when(request.getHeader("Source")).thenReturn("https://remote.example/test/example");
    when(pathResolver.pathExists("/test/example")).thenReturn(true);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.create:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    when(pathResolver.pathExists("/test/example")).thenReturn(false);
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

  @Test
  void testPushTpcRequiresRead() {
    when(request.getMethod()).thenReturn(COPY_METHOD);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.create:/ storage.modify:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

  @Test
  void testMoveRequiresModify() {
    when(request.getMethod()).thenReturn(MOVE_METHOD);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.create:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

  @Test
  void testModifyImpliesCreate() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.modify:/");
    when(pathResolver.pathExists("/test/example")).thenReturn(false);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  void testUnsupportedMethod() {
    when(request.getMethod()).thenReturn("TRACE");
    assertThrows(IllegalArgumentException.class, () -> {
      pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    });
  }

  @Test
  void testPathAuthzIsEnforced() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/subfolder");
    when(request.getMethod()).thenReturn("GET");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));

  }

  @Test
  void issuerChecksAreEnforced() throws Exception {
    when(jwt.getIssuer()).thenReturn(new URL("https://unknown.example"));
    when(request.getMethod()).thenReturn("GET");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Unknown token issuer"));
  }
}
