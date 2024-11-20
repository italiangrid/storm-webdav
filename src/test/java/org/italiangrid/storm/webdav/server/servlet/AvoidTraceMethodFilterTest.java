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
package org.italiangrid.storm.webdav.server.servlet;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;

import org.italiangrid.storm.webdav.test.utils.voms.WithMockVOMSUser;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@WithAnonymousUser
class AvoidTraceMethodFilterTest {

  static final HttpMethod TRACK_HTTP_METHOD = HttpMethod.valueOf("TRACK");

  @Autowired
  MockMvc mvc;

  @Test
  void traceAsAnonymousLeadsTo405() throws Exception {
    mvc.perform(MockMvcRequestBuilders.request(HttpMethod.TRACE, "/test/file"))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @WithMockVOMSUser(vos = "wlcg", saReadPermissions = {"wlcg"})
  void traceAsNonAnonymousLeadsTo405() throws Exception {
    mvc.perform(MockMvcRequestBuilders.request(HttpMethod.TRACE, "/wlcg/file"))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void traceAsAnonymousOnRootLeadsTo405() throws Exception {
    mvc.perform(MockMvcRequestBuilders.request(HttpMethod.TRACE, "/"))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @WithMockVOMSUser(vos = "wlcg", saReadPermissions = {"wlcg"})
  void traceAsNonAnonymousOnRootLeadsTo405() throws Exception {
    mvc.perform(MockMvcRequestBuilders.request(HttpMethod.TRACE, "/"))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void trackAsAnonymousLeadsTo405() throws Exception {
    mvc.perform(MockMvcRequestBuilders.request(TRACK_HTTP_METHOD, new URI("/test/file")))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @WithMockVOMSUser(vos = "wlcg", saReadPermissions = {"wlcg"})
  void trackAsNonAnonymousLeadsTo405() throws Exception {
    mvc.perform(MockMvcRequestBuilders.request(TRACK_HTTP_METHOD, new URI("/wlcg/file")))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void trackAsAnonymousOnRootLeadsTo405() throws Exception {
    mvc.perform(MockMvcRequestBuilders.request(TRACK_HTTP_METHOD, new URI("/")))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @WithMockVOMSUser(vos = "wlcg", saReadPermissions = {"wlcg"})
  void trackAsNonAnonymousOnRootLeadsTo405() throws Exception {
    mvc.perform(MockMvcRequestBuilders.request(TRACK_HTTP_METHOD, new URI("/")))
      .andExpect(status().isMethodNotAllowed());
  }

}
