// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.macaroon;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.macaroon.MacaroonRequestFilter;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "lhcb"})
@WithAnonymousUser
public class MacaroonRequestLhcbIntegrationTests {

  public static final Instant NOW = Instant.parse("2018-01-01T00:00:00.00Z");
  public static final Instant NOW_PLUS_2H =
      NOW.plusSeconds(TimeUnit.HOURS.toSeconds(2)).truncatedTo(ChronoUnit.SECONDS);

  public static final String EMPTY_JSON_OBJECT = "{}";

  @TestConfiguration
  static class Configuration {
    @Bean
    @Primary
    Clock mockClock() {
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
  @WithMockVOMSUser(
      saReadPermissions = {"lhcb_disk"},
      vos = {"lhcb"})
  void macaroonIssuedWithNoWritePermissions() throws Exception {
    mvc.perform(
            post("/disk/lhcb/source")
                .contentType(MacaroonRequestFilter.MACAROON_REQUEST_CONTENT_TYPE)
                .content(EMPTY_JSON_OBJECT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.macaroon").exists());
  }
}
