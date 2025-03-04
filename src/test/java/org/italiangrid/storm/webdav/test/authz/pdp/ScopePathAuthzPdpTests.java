// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.authz.pdp;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;
import static org.italiangrid.storm.webdav.authz.pdp.WlcgStructuredPathAuthorizationPdp.SCOPE_CLAIM;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

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
  void setup() throws MalformedURLException {
    jwtAuth = new JwtAuthenticationToken(jwt);
    lenient().when(jwt.getIssuer()).thenReturn(new URL("https://issuer.example"));
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("storage.read:/");
    lenient().when(request.getServletPath()).thenReturn("/");
    lenient().when(request.getPathInfo()).thenReturn("test/example");
    lenient().when(sa.rootPath()).thenReturn("/storage");
    lenient().when(sa.accessPoints()).thenReturn(List.of("/test"));
    lenient().when(sa.orgs()).thenReturn(Set.of("https://issuer.example"));
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
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn(null);
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.INDETERMINATE));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("no scope"));
  }

  @Test
  void noSaYeldsIndeterminate() {

    lenient().when(pathResolver.resolveStorageArea("/test/example")).thenReturn(null);
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.INDETERMINATE));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("No storage area"));
  }

  @Test
  void noStorageScopesYeldsDeny() {
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid profile storage.read");

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.INDETERMINATE));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
  }

  @Test
  void noStorageScopesYeldsDenyForCatchallMethods() {
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid profile");

    for (String m : CATCHALL_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

      assertThat(result.getDecision(), is(Decision.INDETERMINATE));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }
  }

  @Test
  void catchallMethodsRequestsAtLeastOneStorageScope() {
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : CATCHALL_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : CATCHALL_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.create:/");

    for (String m : CATCHALL_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.stage:/");

    for (String m : CATCHALL_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  void readMethodsRequestsRequireStorageReadOrStage() {
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : READ_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : READ_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.stage:/");

    for (String m : READ_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  void replaceMethodsRequestsRequireStorageModifyOrCreate() {
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : REPLACE_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM))
      .thenReturn("openid storage.read:/ storage.create:/");
    lenient().when(pathResolver.pathExists("/test/example")).thenReturn(true);

    for (String m : REPLACE_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    lenient().when(pathResolver.pathExists("/test/example")).thenReturn(false);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : REPLACE_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  void modifyMethodsRequestsRequireStorageModifyOrCreate() {
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM))
      .thenReturn("openid storage.read:/ storage.create:/");

    for (String m : MODIFY_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM))
      .thenReturn("openid storage.read:/ storage.modify:/");

    for (String m : MODIFY_METHODS) {
      lenient().when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  void testLocalCopyRequiresStorageCreateOrModify() {

    lenient().when(request.getMethod()).thenReturn(COPY_METHOD);
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM))
      .thenReturn("openid storage.read:/ storage.write:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

  @Test
  void testPullTpcRequiresCreateOrModify() {
    lenient().when(request.getMethod()).thenReturn(COPY_METHOD);
    lenient().when(request.getHeader("Source")).thenReturn("https://remote.example/test/example");
    lenient().when(pathResolver.pathExists("/test/example")).thenReturn(true);
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.create:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    lenient().when(pathResolver.pathExists("/test/example")).thenReturn(false);
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

  @Test
  void testPushTpcRequiresRead() {
    lenient().when(request.getMethod()).thenReturn(COPY_METHOD);
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM))
      .thenReturn("openid storage.create:/ storage.modify:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

  @Test
  void testMoveRequiresModify() {
    lenient().when(request.getMethod()).thenReturn(MOVE_METHOD);
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM))
      .thenReturn("openid storage.read:/ storage.create:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

  @Test
  void testModifyImpliesCreate() {
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM))
      .thenReturn("openid storage.read:/ storage.modify:/");
    lenient().when(pathResolver.pathExists("/test/example")).thenReturn(false);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  void testUnsupportedMethod() {
    lenient().when(request.getMethod()).thenReturn("TRACE");
    assertThrows(IllegalArgumentException.class, () -> {
      pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    });
  }

  @Test
  void testPathAuthzIsEnforced() {
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/subfolder");
    lenient().when(request.getMethod()).thenReturn("GET");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));

  }

  @Test
  void issuerChecksAreEnforced() throws Exception {
    lenient().when(jwt.getIssuer()).thenReturn(new URL("https://unknown.example"));
    lenient().when(request.getMethod()).thenReturn("GET");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Unknown token issuer"));
  }

  @Test
  void parentDirCreationIsAllowedWithStorageCreateOrModify() {

    lenient().when(pathResolver.resolveStorageArea(anyString())).thenReturn(sa);
    lenient().when(request.getPathInfo()).thenReturn("test/dir/subdir");
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM))
      .thenReturn("openid storage.create:/dir/subdir");
    lenient().when(request.getMethod()).thenReturn("MKCOL");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));

    lenient().when(request.getPathInfo()).thenReturn("test/dir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM))
      .thenReturn("openid storage.modify:/dir/subdir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));

    lenient().when(request.getPathInfo()).thenReturn("test/dir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }

  @Test
  void parentDirCreationIsNotAllowedWithWrongScopes() {

    lenient().when(pathResolver.resolveStorageArea(anyString())).thenReturn(sa);
    lenient().when(request.getPathInfo()).thenReturn("test/dir/subdir");
    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/dir/subdir");
    lenient().when(request.getMethod()).thenReturn("MKCOL");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));

    lenient().when(request.getPathInfo()).thenReturn("test/dir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));

    lenient().when(jwt.getClaimAsString(SCOPE_CLAIM))
      .thenReturn("openid storage.stage:/dir/subdir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));

    lenient().when(request.getPathInfo()).thenReturn("test/dir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));
  }
}
