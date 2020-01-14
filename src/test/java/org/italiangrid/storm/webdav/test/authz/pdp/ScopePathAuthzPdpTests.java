/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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
import static org.italiangrid.storm.webdav.authz.pdp.WlcgStructuredPathAuthorizationPdp.SCOPE_CLAIM;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.Decision;
import org.italiangrid.storm.webdav.authz.pdp.WlcgStructuredPathAuthorizationPdp;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ScopePathAuthzPdpTests {

  public static final String[] READ_METHODS = {"GET", "PROPFIND", "OPTIONS", "HEAD"};
  public static final String[] REPLACE_METHODS = {"PUT", "MKCOL"};
  public static final String[] MODIFY_METHODS = {"DELETE", "PATCH"};
  public static final String COPY_METHOD = "COPY";
  public static final String MOVE_METHOD = "MOVE";

  @Mock
  PathResolver pathResolver;

  @Mock
  LocalURLService localUrlService;

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

  @Before
  public void setup() {
    jwtAuth = new JwtAuthenticationToken(jwt);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("storage.read:/");
    when(request.getServletPath()).thenReturn("/");
    when(request.getPathInfo()).thenReturn("test/example");
    when(sa.accessPoints()).thenReturn(Lists.newArrayList("/test"));
    when(pathResolver.resolveStorageArea("/test/example")).thenReturn(sa);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidAuthentication() throws Exception {

    Authentication auth = mock(Authentication.class);
    pdp.authorizeRequest(newAuthorizationRequest(request, auth));
  }

  @Test
  public void noScopeClaimYeldsIndeterminate() throws Exception {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn(null);
    PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.INDETERMINATE));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("no scope"));
  }

  @Test
  public void noSaYeldsIndeterminate() throws Exception {

    when(pathResolver.resolveStorageArea("/test/example")).thenReturn(null);
    PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.INDETERMINATE));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("No storage area"));
  }

  @Test
  public void noStorageScopesYeldsDeny() throws Exception {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid profile storage.read");

    PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));

    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
  }


  @Test
  public void readMethodsRequestsRequireStorageRead() throws Exception {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : READ_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : READ_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  public void replaceMethodsRequestsRequireStorageModifyOrCreate() throws Exception {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.create:/");
    when(pathResolver.pathExists("/test/example")).thenReturn(true);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    when(pathResolver.pathExists("/test/example")).thenReturn(false);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  public void modifyMethodsRequestsRequireStorageModifyOrCreate() throws Exception {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.create:/");

    for (String m : MODIFY_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.DENY));
      assertThat(result.getMessage().isPresent(), is(true));
      assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    }

    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.modify:/");

    for (String m : MODIFY_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }

  @Test
  public void testLocalCopyRequiresStorageRead() throws Exception {

    when(request.getMethod()).thenReturn(COPY_METHOD);
    when(request.getHeader("Destination")).thenReturn("https://test.example/test/example.2");
    when(localUrlService.isLocalURL("https://test.example/test/example.2")).thenReturn(true);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    
    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
   
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }
  
  @Test
  public void testPullTpcRequiresCreateOrModify() throws Exception{
    when(request.getMethod()).thenReturn(COPY_METHOD);
    when(request.getHeader("Source")).thenReturn("https://remote.example/test/example");
    when(pathResolver.pathExists("/test/example")).thenReturn(true);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    
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
  public void testPushTpcRequiresRead() throws Exception {
    when(request.getMethod()).thenReturn(COPY_METHOD);
    when(request.getHeader("Destination")).thenReturn("https://remote.example/test/example");
    when(localUrlService.isLocalURL("https://remote.example/test/example")).thenReturn(false);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.create:/ storage.modify:/");
    PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    
    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
  }
  
  @Test
  public void testMoveRequiresModify() throws Exception {
    when(request.getMethod()).thenReturn(MOVE_METHOD);
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.create:/");
    PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    
    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.modify:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT)); 
  }
  
  @Test
  public void testModifyImpliesCreate() throws Exception {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/ storage.modify:/");
    when(pathResolver.pathExists("/test/example")).thenReturn(false);

    for (String m : REPLACE_METHODS) {
      when(request.getMethod()).thenReturn(m);
      PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
      assertThat(result.getDecision(), is(Decision.PERMIT));
    }
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testUnsupportedMethod() throws Exception {
    when(request.getMethod()).thenReturn("TRACE");
    pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
  }
  
  @Test
  public void testPathAuthzIsEnforced() throws Exception {
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/subfolder");
    when(request.getMethod()).thenReturn("GET");
    PathAuthorizationResult result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.DENY));
    assertThat(result.getMessage().isPresent(), is(true));
    assertThat(result.getMessage().get(), containsString("Insufficient token scope"));
    
    when(jwt.getClaimAsString(SCOPE_CLAIM)).thenReturn("openid storage.read:/");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, jwtAuth));
    assertThat(result.getDecision(), is(Decision.PERMIT));
    
  }
}
