// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.authz.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.milton.http.HttpManager;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.oauth.StormJwtAuthoritiesConverter;
import org.italiangrid.storm.webdav.oauth.authzserver.jwt.SignedJwtTokenIssuer;
import org.italiangrid.storm.webdav.server.servlet.MiltonFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local-authz-test")
@WithAnonymousUser
public class LocalAuthzIntegrationTests {

  public static final String SLASH_WLCG_SLASH_FILE = "/wlcg/file";
  public static final String SLASH_ANONYMOUS_SLASH_FILE = "/anonymous/file";

  public static final String UNKNOWN_ISSUER = "https://unknown.example";
  public static final String LOCAL_ISSUER = "https://issuer.example";

  @Autowired MockMvc mvc;

  @Autowired FilterRegistrationBean<MiltonFilter> miltonFilter;

  @Autowired StormJwtAuthoritiesConverter authConverter;

  @Autowired VOMSAuthenticationFilter filter;

  @Autowired SignedJwtTokenIssuer tokenIssuer;

  @BeforeEach
  void setup() {
    filter.setCheckForPrincipalChanges(false);

    HttpManager httpManager = Mockito.mock(HttpManager.class);
    miltonFilter.getFilter().setMiltonHTTPManager(httpManager);
  }

  @Test
  void testLocalAuthz() throws Exception {
    mvc.perform(put(SLASH_WLCG_SLASH_FILE)).andExpect(status().isUnauthorized());
  }

  @Test
  void testInvalidTokenAuthz() throws Exception {
    Jwt token =
        Jwt.withTokenValue("test")
            .header("kid", "rsa1")
            .issuer(UNKNOWN_ISSUER)
            .subject("123")
            .build();

    mvc.perform(get(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token)))
        .andExpect(status().isForbidden());

    token =
        Jwt.withTokenValue("test")
            .header("kid", "rsa1")
            .issuer(LOCAL_ISSUER)
            .subject("123")
            .build();

    mvc.perform(get(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token)))
        .andExpect(status().isForbidden());
  }

  @Test
  void testValidLocalTokenAuthz() throws Exception {
    Jwt token =
        Jwt.withTokenValue("test")
            .header("kid", "rsa1")
            .issuer(LOCAL_ISSUER)
            .subject("123")
            .claim("path", SLASH_WLCG_SLASH_FILE)
            .claim("perms", "r")
            .build();

    mvc.perform(get(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token))).andExpect(status().isNotFound());

    mvc.perform(put(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token)))
        .andExpect(status().isForbidden());
  }

  @Test
  void testInvalidPathLocalTokenAuthz() throws Exception {
    Jwt token =
        Jwt.withTokenValue("test")
            .header("kid", "rsa1")
            .issuer(LOCAL_ISSUER)
            .subject("123")
            .claim("path", SLASH_ANONYMOUS_SLASH_FILE)
            .claim("perms", "r")
            .build();

    mvc.perform(get(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token)))
        .andExpect(status().isForbidden());
  }

  @Test
  void testInvalidLocalToken() throws Exception {
    Jwt token =
        Jwt.withTokenValue("test")
            .header("kid", "rsa1")
            .issuer(LOCAL_ISSUER)
            .subject("123")
            .build();

    mvc.perform(get(SLASH_WLCG_SLASH_FILE).with(jwt().jwt(token)))
        .andExpect(status().isForbidden());
  }
}
