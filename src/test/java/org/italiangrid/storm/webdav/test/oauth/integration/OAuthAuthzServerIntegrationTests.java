// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.oauth.integration;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.italiangrid.storm.webdav.oauth.authzserver.ErrorResponseDTO.UNSUPPORTED_GRANT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.apache.commons.lang3.RandomStringUtils;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest;
import org.italiangrid.storm.webdav.test.utils.voms.WithMockVOMSUser;
import org.italiangrid.storm.webdav.web.PathConstants;
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

  @Autowired MockMvc mvc;

  @Autowired VOMSAuthenticationFilter filter;

  @Autowired ServiceConfigurationProperties props;

  @Autowired ObjectMapper mapper;

  @BeforeEach
  void setup() {
    filter.setCheckForPrincipalChanges(false);
  }

  @Test
  void getNotSupported() throws Exception {
    mvc.perform(get(PathConstants.OAUTH_TOKEN_PATH).contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void postNotSupportedForAnonymous() throws Exception {
    mvc.perform(post(PathConstants.OAUTH_TOKEN_PATH).contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "test")
  void postNotSupportedForAuthenticatedNonVomsUsers() throws Exception {
    mvc.perform(
            post(PathConstants.OAUTH_TOKEN_PATH)
                .content(CONTENT)
                .contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockVOMSUser(acExpirationSecs = 200)
  void postSupportedForAuthenticatedVomsUsers() throws Exception {
    mvc.perform(
            post(PathConstants.OAUTH_TOKEN_PATH)
                .content(CONTENT)
                .contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").exists())
        .andExpect(jsonPath("$.expires_in", is(200)))
        .andExpect(jsonPath("$.token_type", is("Bearer")));
  }

  @Test
  @WithMockVOMSUser
  void invalidGrantTypeRejected() throws Exception {
    mvc.perform(
            post(PathConstants.OAUTH_TOKEN_PATH)
                .content(CONTENT_CUSTOM)
                .contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is(UNSUPPORTED_GRANT_TYPE)))
        .andExpect(jsonPath("$.error_description", is("Invalid grant type: " + CUSTOM_GRANT_TYPE)))
        .andDo(print());
  }

  @Test
  @WithMockVOMSUser
  void requestedLifetimeHonoured() throws Exception {
    mvc.perform(
            post(PathConstants.OAUTH_TOKEN_PATH)
                .content(format("%s&lifetime=50", CONTENT))
                .contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").exists())
        .andExpect(jsonPath("$.expires_in", is(50)))
        .andExpect(jsonPath("$.token_type", is("Bearer")));
  }

  @Test
  @WithMockVOMSUser(acExpirationSecs = 200)
  void requestedLifetimeLimited() throws Exception {
    mvc.perform(
            post(PathConstants.OAUTH_TOKEN_PATH)
                .content(format("%s&lifetime=200000", CONTENT))
                .contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").exists())
        .andExpect(jsonPath("$.expires_in", is(200)))
        .andExpect(jsonPath("$.token_type", is("Bearer")));
  }

  @Test
  @WithMockVOMSUser
  void scopeLengthIsChecked() throws Exception {

    RandomStringUtils randomStringUtils = RandomStringUtils.insecure();

    String randomAlphabetic = randomStringUtils.nextAlphabetic(AccessTokenRequest.MAX_SCOPE_LENGTH);

    mvc.perform(
            post(PathConstants.OAUTH_TOKEN_PATH)
                .content(format("%s&scope=%s", CONTENT, randomAlphabetic))
                .contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").exists());

    randomAlphabetic = randomStringUtils.nextAlphabetic(AccessTokenRequest.MAX_SCOPE_LENGTH + 1);

    mvc.perform(
            post(PathConstants.OAUTH_TOKEN_PATH)
                .content(format("%s&scope=%s", CONTENT, randomAlphabetic))
                .contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid_scope")))
        .andExpect(jsonPath("$.error_description", is(AccessTokenRequest.SCOPE_TOO_LONG)));
  }
}
