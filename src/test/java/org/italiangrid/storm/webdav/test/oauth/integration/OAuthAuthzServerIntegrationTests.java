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
package org.italiangrid.storm.webdav.test.oauth.integration;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.CoreMatchers.is;
import static org.italiangrid.storm.webdav.oauth.authzserver.ErrorResponseDTO.UNSUPPORTED_GRANT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest;
import org.italiangrid.storm.webdav.test.utils.voms.WithMockVOMSUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@WithAnonymousUser
public class OAuthAuthzServerIntegrationTests {

  public static final Instant NOW = Instant.parse("2018-01-01T00:00:00.00Z");

  public static final String GRANT_TYPE = "grant_type";
  public static final String CLIENT_CREDENTIALS = "client_credentials";
  public static final String CUSTOM_GRANT_TYPE = "my_own_grant_type";
  public static final String CONTENT = format("%s=%s", GRANT_TYPE, CLIENT_CREDENTIALS);
  public static final String CONTENT_CUSTOM = format("%s=%s", GRANT_TYPE, CUSTOM_GRANT_TYPE);

  @TestConfiguration
  static class Configuration {
    @Bean
    @Primary
    public Clock mockClock() {
      return Clock.fixed(NOW, ZoneId.systemDefault());
    }
  }

  @Autowired
  MockMvc mvc;

  @Autowired
  VOMSAuthenticationFilter filter;

  @Autowired
  ServiceConfigurationProperties props;

  @Autowired
  ObjectMapper mapper;

  @BeforeEach
  public void setup() {
    filter.setCheckForPrincipalChanges(false);
  }

  @Test
  void getNotSupported() throws Exception {
    mvc.perform(get("/oauth/token").contentType(APPLICATION_FORM_URLENCODED))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void postNotSupportedForAnonymous() throws Exception {
    mvc.perform(post("/oauth/token").contentType(APPLICATION_FORM_URLENCODED))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "test")
  void postNotSupportedForAuthenticatedNonVomsUsers() throws Exception {
    mvc.perform(post("/oauth/token").content(CONTENT).contentType(APPLICATION_FORM_URLENCODED))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockVOMSUser(acExpirationSecs = 200)
  void postSupportedForAuthenticatedVomsUsers() throws Exception {
    mvc.perform(post("/oauth/token").content(CONTENT).contentType(APPLICATION_FORM_URLENCODED))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.access_token").exists())
      .andExpect(jsonPath("$.expires_in", is(200)))
      .andExpect(jsonPath("$.token_type", is("Bearer")));
  }

  @Test
  @WithMockVOMSUser
  void invalidGrantTypeRejected() throws Exception {
    mvc
      .perform(
          post("/oauth/token").content(CONTENT_CUSTOM).contentType(APPLICATION_FORM_URLENCODED))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error", is(UNSUPPORTED_GRANT_TYPE)))
      .andExpect(jsonPath("$.error_description", is("Invalid grant type: " + CUSTOM_GRANT_TYPE)))
      .andDo(print());
  }

  @Test
  @WithMockVOMSUser
  void requestedLifetimeHonoured() throws Exception {
    mvc
      .perform(post("/oauth/token").content(format("%s&lifetime=50", CONTENT))
        .contentType(APPLICATION_FORM_URLENCODED))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.access_token").exists())
      .andExpect(jsonPath("$.expires_in", is(50)))
      .andExpect(jsonPath("$.token_type", is("Bearer")));
  }

  @Test
  @WithMockVOMSUser(acExpirationSecs = 200)
  void requestedLifetimeLimited() throws Exception {
    mvc
      .perform(post("/oauth/token").content(format("%s&lifetime=200000", CONTENT))
        .contentType(APPLICATION_FORM_URLENCODED))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.access_token").exists())
      .andExpect(jsonPath("$.expires_in", is(200)))
      .andExpect(jsonPath("$.token_type", is("Bearer")));
  }

  @Test
  @WithMockVOMSUser
  void scopeLengthIsChecked() throws Exception {

    String randomAlphabetic = randomAlphabetic(AccessTokenRequest.MAX_SCOPE_LENGTH);

    mvc
      .perform(post("/oauth/token").content(format("%s&scope=%s", CONTENT, randomAlphabetic))
        .contentType(APPLICATION_FORM_URLENCODED))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.access_token").exists());

    randomAlphabetic = randomAlphabetic(AccessTokenRequest.MAX_SCOPE_LENGTH + 1);

    mvc
      .perform(post("/oauth/token").content(format("%s&scope=%s", CONTENT, randomAlphabetic))
        .contentType(APPLICATION_FORM_URLENCODED))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error", is("invalid_scope")))
      .andExpect(jsonPath("$.error_description", is(AccessTokenRequest.SCOPE_TOO_LONG)));
  }

}
