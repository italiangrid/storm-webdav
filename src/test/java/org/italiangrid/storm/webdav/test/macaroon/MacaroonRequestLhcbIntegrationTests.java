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
package org.italiangrid.storm.webdav.test.macaroon;

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

import com.fasterxml.jackson.databind.ObjectMapper;

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

  public MacaroonRequestLhcbIntegrationTests() {
    // TODO Auto-generated constructor stub
  }

  @Test
  @WithMockVOMSUser(saReadPermissions = {"lhcb_disk"}, vos = {"lhcb"})
  public void macaroonIssuedWithNoWritePermissions() throws Exception {
    mvc
      .perform(post("/disk/lhcb/source").contentType(MacaroonRequestFilter.MACAROON_REQUEST_CONTENT_TYPE)
        .content(EMPTY_JSON_OBJECT))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.macaroon").exists());
  }

}
