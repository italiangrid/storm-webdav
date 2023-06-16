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

package org.italiangrid.storm.webdav.test.tape;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev"})
@WithAnonymousUser
class WlcgTapeRestApiEndpointTest {

  @Autowired
  MockMvc mvc;

  @Test
  void testEnabledWellKnown() throws Exception {
    mvc.perform(get("/.well-known/wlcg-tape-rest-api"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.sitename").exists())
      .andExpect(jsonPath("$.sitename").value(is("StoRM@CNAF")))
      .andExpect(jsonPath("$.description").exists())
      .andExpect(jsonPath("$.description").value(is("This is the tape REST API endpoint for CNAF-T1")))
      .andExpect(jsonPath("$.endpoints").exists())
      .andExpect(jsonPath("$.endpoints").isArray())
      .andExpect(jsonPath("$.endpoints").isNotEmpty())
      .andExpect(jsonPath("$.endpoints[0].uri").value(is("https://storm-tape.example.org:8443/api/v1")))
      .andExpect(jsonPath("$.endpoints[0].version").value(is("v1")))
      .andExpect(jsonPath("$.endpoints[0].metadata").isEmpty());

  }
}
