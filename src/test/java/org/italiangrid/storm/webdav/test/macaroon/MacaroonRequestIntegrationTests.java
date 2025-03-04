// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.macaroon;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.macaroon.MacaroonRequestDTO;
import org.italiangrid.storm.webdav.macaroon.MacaroonRequestFilter;
import org.italiangrid.storm.webdav.macaroon.MacaroonResponseDTO;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@WithAnonymousUser
public class MacaroonRequestIntegrationTests {

  public static final Instant NOW = Instant.parse("2018-01-01T00:00:00.00Z");
  public static final Instant NOW_PLUS_2H =
      NOW.plusSeconds(TimeUnit.HOURS.toSeconds(2)).truncatedTo(ChronoUnit.SECONDS);

  public static final String EMPTY_JSON_OBJECT = "{}";

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
  void setup() {
    filter.setCheckForPrincipalChanges(false);
  }

  public MacaroonRequestIntegrationTests() {
    // TODO Auto-generated constructor stub
  }

  @Test
  void getNotSupported() throws Exception {
    mvc.perform(get("/whatever").contentType(MacaroonRequestFilter.MACAROON_REQUEST_CONTENT_TYPE))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void emptyRequestFails() throws Exception {
    mvc.perform(post("/whatever").contentType(MacaroonRequestFilter.MACAROON_REQUEST_CONTENT_TYPE))
      .andExpect(status().isBadRequest());
  }

  @Test
  void vomsRequired() throws Exception {
    mvc
      .perform(post("/whatever").contentType(MacaroonRequestFilter.MACAROON_REQUEST_CONTENT_TYPE)
        .content(EMPTY_JSON_OBJECT))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockVOMSUser
  void macaroonIssued() throws Exception {
    mvc
      .perform(post("/whatever").contentType(MacaroonRequestFilter.MACAROON_REQUEST_CONTENT_TYPE)
        .content(EMPTY_JSON_OBJECT))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.macaroon").exists());
  }


  @Test
  @WithMockVOMSUser(acExpirationSecs = 43200)
  void validityEnforced() throws Exception {

    MacaroonRequestDTO dto = new MacaroonRequestDTO();
    dto.setValidity("PT2H");

    String response = mvc
      .perform(post("/whatever").contentType(MacaroonRequestFilter.MACAROON_REQUEST_CONTENT_TYPE)
        .content(mapper.writeValueAsString(dto)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.macaroon").exists())
      .andReturn()
      .getResponse()
      .getContentAsString();

    MacaroonResponseDTO res = mapper.readValue(response, MacaroonResponseDTO.class);

    SignedJWT signedJwt = SignedJWT.parse(res.getMacaroon());

    assertThat(signedJwt.getJWTClaimsSet().getExpirationTime().toInstant(), is(NOW_PLUS_2H));

  }
}
