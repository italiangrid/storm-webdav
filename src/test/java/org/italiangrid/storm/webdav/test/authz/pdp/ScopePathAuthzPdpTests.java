// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.authz.pdp;

import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;
import static org.italiangrid.storm.webdav.authz.pdp.WlcgStructuredPathAuthorizationPdp.SCOPE_CLAIM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
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
class ScopePathAuthzPdpTests {

  public static final String[] CATCHALL_METHODS = {"HEAD", "OPTIONS"};
  public static final String[] READ_METHODS = {"GET", "PROPFIND"};
  public static final String[] REPLACE_METHODS = {"PUT", "MKCOL"};
  public static final String[] MODIFY_METHODS = {"DELETE", "PATCH"};
  public static final String COPY_METHOD = "COPY";
  public static final String MOVE_METHOD = "MOVE";

  @Mock PathResolver pathResolver;

  @Mock LocalURLService localUrlService;

  @Mock Enumeration<String> requestHeaderNames;

  @Spy ServiceConfigurationProperties config = new ServiceConfigurationProperties();

  @Mock HttpServletRequest request;

  @Mock Jwt jwt;

  @Mock StorageAreaInfo sa;

  JwtAuthenticationToken jwtAuth;

  @InjectMocks WlcgStructuredPathAuthorizationPdp pdp;

  @BeforeEach
  void setup() throws MalformedURLException, URISyntaxException {
    jwtAuth = new JwtAuthenticationToken(jwt);
    lenient().when(jwt.getIssuer()).thenReturn(new URI("https://issuer.example").toURL());
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
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          pdp.authorizeRequest(newAuthorizationRequest(request, auth));
        });
  }

  @Test
  void noScopeClaimYeldsIndeterminate() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn(null);
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.INDETERMINATE, result.getDecision());
    assertTrue(result.getMessage().isPresent());
    assertTrue(result.getMessage().get().contains("no scope"));
  }

  @Test
  void noSaYeldsIndeterminate() {

    when(pathResolver.resolveStorageArea("/test/example")).thenReturn(null);
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.INDETERMINATE, result.getDecision());
    assertTrue(result.getMessage().isPresent());
    assertTrue(result.getMessage().get().contains("No storage area"));
  }

  @Test
  void noStorageScopesYeldsDeny() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid profile storage.read");

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertEquals(Decision.INDETERMINATE, result.getDecision());
    assertTrue(result.getMessage().isPresent());
    assertTrue(result.getMessage().get().contains("Insufficient token scope"));
  }

  @Test
  void noStorageScopesYeldsDenyForCatchallMethods() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid profile");

    for (String m : CATCHALL_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

      assertEquals(Decision.INDETERMINATE, result.getDecision());
      assertTrue(result.getMessage().isPresent());
      assertTrue(result.getMessage().get().contains("Insufficient token scope"));
    }
  }

  @Test
  void catchallMethodsRequestsAtLeastOneStorageScope() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : CATCHALL_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.PERMIT, result.getDecision());
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : CATCHALL_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.PERMIT, result.getDecision());
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.create:/");

    for (String m : CATCHALL_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.PERMIT, result.getDecision());
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.stage:/");

    for (String m : CATCHALL_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.PERMIT, result.getDecision());
    }
  }

  @Test
  void readMethodsRequestsRequireStorageReadOrStage() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : READ_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.DENY, result.getDecision());
      assertTrue(result.getMessage().isPresent());
      assertTrue(result.getMessage().get().contains("Insufficient token scope"));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : READ_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.PERMIT, result.getDecision());
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.stage:/");

    for (String m : READ_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.PERMIT, result.getDecision());
    }
  }

  @Test
  void replaceMethodsRequestsRequireStorageModifyOrCreate() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.DENY, result.getDecision());
      assertTrue(result.getMessage().isPresent());
      assertTrue(result.getMessage().get().contains("Insufficient token scope"));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.create:/");
    when(pathResolver.pathExists("/test/example")).thenReturn(true);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.DENY, result.getDecision());
      assertTrue(result.getMessage().isPresent());
      assertTrue(result.getMessage().get().contains("Insufficient token scope"));
    }

    when(pathResolver.pathExists("/test/example")).thenReturn(false);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.PERMIT, result.getDecision());
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.PERMIT, result.getDecision());
    }
  }

  @Test
  void modifyMethodsRequestsRequireStorageModifyOrCreate() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.create:/");

    for (String m : MODIFY_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.DENY, result.getDecision());
      assertTrue(result.getMessage().isPresent());
      assertTrue(result.getMessage().get().contains("Insufficient token scope"));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.modify:/");

    for (String m : MODIFY_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.PERMIT, result.getDecision());
    }
  }

  @Test
  void testLocalCopyRequiresStorageCreateOrModify() {

    when(request.getMethod()).thenReturn(COPY_METHOD);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertEquals(Decision.DENY, result.getDecision());
    assertTrue(result.getMessage().isPresent());
    assertTrue(result.getMessage().get().contains("Insufficient token scope"));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.write:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.PERMIT, result.getDecision());
  }

  @Test
  void testPullTpcRequiresCreateOrModify() {
    when(request.getMethod()).thenReturn(COPY_METHOD);
    when(request.getHeader("Source")).thenReturn("https://remote.example/test/example");
    when(pathResolver.pathExists("/test/example")).thenReturn(true);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertEquals(Decision.DENY, result.getDecision());
    assertTrue(result.getMessage().isPresent());
    assertTrue(result.getMessage().get().contains("Insufficient token scope"));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.create:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.DENY, result.getDecision());
    assertTrue(result.getMessage().isPresent());
    assertTrue(result.getMessage().get().contains("Insufficient token scope"));

    when(pathResolver.pathExists("/test/example")).thenReturn(false);
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.PERMIT, result.getDecision());

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.PERMIT, result.getDecision());
  }

  @Test
  void testPushTpcRequiresRead() {
    when(request.getMethod()).thenReturn(COPY_METHOD);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.create:/ storage.modify:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertEquals(Decision.DENY, result.getDecision());
    assertTrue(result.getMessage().isPresent());
    assertTrue(result.getMessage().get().contains("Insufficient token scope"));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.PERMIT, result.getDecision());
  }

  @Test
  void testMoveRequiresModify() {
    when(request.getMethod()).thenReturn(MOVE_METHOD);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.create:/");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertEquals(Decision.DENY, result.getDecision());
    assertTrue(result.getMessage().isPresent());
    assertTrue(result.getMessage().get().contains("Insufficient token scope"));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.PERMIT, result.getDecision());
  }

  @Test
  void testModifyImpliesCreate() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.modify:/");
    when(pathResolver.pathExists("/test/example")).thenReturn(false);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result =
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertEquals(Decision.PERMIT, result.getDecision());
    }
  }

  @Test
  void testUnsupportedMethod() {
    when(request.getMethod()).thenReturn("TRACE");
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
        });
  }

  @Test
  void testPathAuthzIsEnforced() {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/subfolder");
    when(request.getMethod()).thenReturn("GET");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.DENY, result.getDecision());
    assertTrue(result.getMessage().isPresent());
    assertTrue(result.getMessage().get().contains("Insufficient token scope"));

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.PERMIT, result.getDecision());
  }

  @Test
  void issuerChecksAreEnforced() throws MalformedURLException, URISyntaxException {
    when(jwt.getIssuer()).thenReturn(new URI("https://unknown.example").toURL());
    when(request.getMethod()).thenReturn("GET");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.DENY, result.getDecision());
    assertTrue(result.getMessage().isPresent());
    assertTrue(result.getMessage().get().contains("Unknown token issuer"));
  }

  @Test
  void parentDirCreationIsAllowedWithStorageCreateOrModify() {

    when(pathResolver.resolveStorageArea(anyString())).thenReturn(sa);
    when(request.getPathInfo()).thenReturn("test/dir/subdir");
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.create:/dir/subdir");
    when(request.getMethod()).thenReturn("MKCOL");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.PERMIT, result.getDecision());

    when(request.getPathInfo()).thenReturn("test/dir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.PERMIT, result.getDecision());

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/dir/subdir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.PERMIT, result.getDecision());

    when(request.getPathInfo()).thenReturn("test/dir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.PERMIT, result.getDecision());
  }

  @Test
  void parentDirCreationIsNotAllowedWithWrongScopes() {

    when(pathResolver.resolveStorageArea(anyString())).thenReturn(sa);
    when(request.getPathInfo()).thenReturn("test/dir/subdir");
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/dir/subdir");
    when(request.getMethod()).thenReturn("MKCOL");
    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.DENY, result.getDecision());

    when(request.getPathInfo()).thenReturn("test/dir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.DENY, result.getDecision());

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.stage:/dir/subdir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.DENY, result.getDecision());

    when(request.getPathInfo()).thenReturn("test/dir");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertEquals(Decision.DENY, result.getDecision());
  }

  @Test
  void testEmptyRequestPathInfo() {
    when(request.getPathInfo()).thenReturn(null);
    when(request.getServletPath()).thenReturn("http://unknown.example");
    assertEquals("http://unknown.example", pdp.getRequestPath(request));
  }
}
