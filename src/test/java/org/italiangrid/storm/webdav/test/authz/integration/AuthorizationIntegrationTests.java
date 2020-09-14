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
package org.italiangrid.storm.webdav.test.authz.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;

import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.oauth.StormJwtAuthoritiesConverter;
import org.italiangrid.storm.webdav.server.servlet.MiltonFilter;
import org.italiangrid.storm.webdav.test.utils.voms.WithMockVOMSUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import io.milton.http.HttpManager;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("authz-test")
@WithAnonymousUser
public class AuthorizationIntegrationTests {
  public static final String SLASH_WLCG_SLASH_FILE = "/wlcg/file";
  public static final String SLASH_ANONYMOUS_SLASH_FILE = "/anonymous/file";

  public static final String WLCG_ISSUER = "https://wlcg.cloud.cnaf.infn.it/";
  public static final String UNKNOWN_ISSUER = "https://unknown.example";
  public static final String EXAMPLE_ISSUER = "https://issuer.example";


  @Autowired
  MockMvc mvc;

  @Autowired
  VOMSAuthenticationFilter filter;

  @Autowired
  FilterRegistrationBean<MiltonFilter> miltonFilter;

  @Autowired
  StormJwtAuthoritiesConverter authConverter;

  @Before
  public void setup() {
    filter.setCheckForPrincipalChanges(false);

    HttpManager httpManager = Mockito.mock(HttpManager.class);
    miltonFilter.getFilter().setMiltonHTTPManager(httpManager);
  }

  @Test
  public void anonymousGetAccessToAnonymousSaWorks() throws Exception {
    mvc.perform(get(SLASH_ANONYMOUS_SLASH_FILE)).andExpect(status().isNotFound());
  }

  @Test
  public void anonymousWriteAccessToAnonymousSaIsBlocked() throws Exception {
    mvc.perform(put(SLASH_ANONYMOUS_SLASH_FILE)).andExpect(status().isUnauthorized());
  }

  @Test
  public void writeAccessAsAnonymousLeadsTo401() throws Exception {
    mvc.perform(put(SLASH_WLCG_SLASH_FILE)).andExpect(status().isUnauthorized());
  }

  @WithMockVOMSUser(vos = "wlcg", saReadPermissions = {"wlcg"})
  @Test
  public void getAccessAsVomsUserLeads404() throws Exception {
    mvc.perform(get(SLASH_WLCG_SLASH_FILE)).andExpect(status().isNotFound());
  }

  @WithMockVOMSUser(vos = "wlcg", saReadPermissions = {"wlcg"})
  @Test
  public void putAccessAsVomsUserIsForbidden() throws Exception {
    mvc.perform(put(SLASH_WLCG_SLASH_FILE)).andExpect(status().isForbidden());
  }

  @WithMockVOMSUser(vos = "wlcg", saReadPermissions = {"wlcg"}, saWritePermissions = {"wlcg"})
  @Test
  public void putAccessAsVomsUserIsOk() throws Exception {
    mvc.perform(put(SLASH_WLCG_SLASH_FILE)).andExpect(status().isOk());
  }


  @Test
  public void issuerChecksAreEnforcedForWlcgScopeBasedAuthz() throws Exception {
    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(UNKNOWN_ISSUER)
      .subject("123")
      .claim("scope", "storage.read:/")
      .build();

    mvc.perform(get(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void getAccessAsJwtUserWithoutScopeLeadsToAccessDenied() throws Exception {
    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .build();

    mvc.perform(get(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token)))
      .andExpect(status().isForbidden());

  }

  @Test
  public void getAccessAsJwtUserWithTheRightScopeGrantsAccess() throws Exception {
    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.read:/")
      .build();

    mvc.perform(get(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token))).andExpect(status().isNotFound());

  }


  @Test
  public void getAccessAsJwtWithReadCapabilityForWrongPathResultsInAccessDenied() throws Exception {
    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.read:/other")
      .build();

    mvc.perform(get(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token)))
      .andExpect(status().isForbidden());

  }

  @Test
  public void getAccessAsJwtWithWriteCapabilityResultsInAccessDenied() throws Exception {
    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.modify:/")
      .build();

    mvc.perform(get(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token)))
      .andExpect(status().isForbidden());

  }

  @WithMockVOMSUser(vos = "wlcg", saReadPermissions = {"wlcg"})
  @Test
  public void localVomsCopyRequiresWithReadPermissionsGetsAccessDenied() throws Exception {
    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source")).header("Destination",
        "http://localhost/wlcg/destination"))
      .andExpect(status().isForbidden());
  }

  @WithMockVOMSUser(vos = "wlcg", saWritePermissions = {"wlcg"}, saReadPermissions = {"wlcg"})
  @Test
  public void localVomsCopyRequiresReadAndWritePermissions() throws Exception {
    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source")).header("Destination",
        "http://localhost/wlcg/destination"))
      .andExpect(status().isOk());
  }

  @Test
  public void tpcJwtPullCopyBlockedWithStorageReadScope() throws Exception {
    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.read:/")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Source", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isForbidden());
  }

  @Test
  public void tpcJwtPullCopyRequiresStorageModifyScope() throws Exception {
    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.modify:/")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Source", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isAccepted());
  }

  @Test
  public void tpcJwtPullCopyRequiresStorageModifyScopeWithRightPath() throws Exception {
    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.modify:/subdir storage.read:/")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Source", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isForbidden());
  }


  @Test
  public void tpcJwtLocalCopyRequiresAppropriatePermissions() throws Exception {
    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.read:/")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isForbidden());

    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.modify:/")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isForbidden());

    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.read:/ storage.modify:/")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isOk());

    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.read:/subdir storage.modify:/")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isForbidden());

    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.read:/ storage.modify:/subdir")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isForbidden());

    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.read:/source storage.modify:/destination")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isOk());
  }

  @Test
  public void tpcJwtLocalMoveRequiresAppropriatePermissions() throws Exception {
    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.read:/")
      .build();

    mvc.perform(request("MOVE", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isForbidden());

    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.modify:/")
      .build();

    mvc.perform(request("MOVE", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isOk());


    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "storage.modify:/subdir")
      .build();

    mvc.perform(request("MOVE", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isForbidden());


    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(WLCG_ISSUER)
      .subject("123")
      .claim("scope", "openid storage.modify:/source storage.modify:/destination")
      .build();

    mvc.perform(request("MOVE", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token))).andExpect(status().isOk());
  }

  @Test
  public void tpcJwtFineGrainedAuthzCopyTests() throws Exception {

    Jwt token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(EXAMPLE_ISSUER)
      .claim("scope", "openid")
      .subject("123")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token).authorities(authConverter))).andExpect(status().isForbidden());


    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(EXAMPLE_ISSUER)
      .subject("123")
      .claim("groups", "/example/admins")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token).authorities(authConverter))).andExpect(status().isOk());

    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(EXAMPLE_ISSUER)
      .subject("123")
      .claim("groups", "/example")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token).authorities(authConverter))).andExpect(status().isForbidden());

    token = Jwt.withTokenValue("test")
      .header("kid", "rsa1")
      .issuer(UNKNOWN_ISSUER)
      .subject("123")
      .claim("groups", "/example/admins")
      .build();

    mvc.perform(request("COPY", URI.create("http://localhost/wlcg/source"))
      .header("Destination", "http://localhost/wlcg/destination")
      .with(jwt().jwt(token).authorities(authConverter))).andExpect(status().isForbidden());

  }


  @WithMockVOMSUser(vos = "wlcg", saReadPermissions = {"wlcg"}, saWritePermissions = {"wlcg"})
  @Test
  public void deleteOnStorageAreaRootIsForbidden() throws Exception {
    mvc.perform(delete("/wlcg")).andExpect(status().isForbidden());
  }

}