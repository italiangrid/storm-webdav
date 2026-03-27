// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tape;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev"})
@WithAnonymousUser
class EnabledEndpointTest {

  @Autowired MockMvc mvc;

  @Test
  void testEnabledWellKnown() throws Exception {
    mvc.perform(get("/.well-known/wlcg-tape-rest-api"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sitename").exists())
        .andExpect(jsonPath("$.sitename").value("StoRM@CNAF"))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(
            jsonPath("$.description").value("This is the tape REST API endpoint for CNAF-T1"))
        .andExpect(jsonPath("$.endpoints").exists())
        .andExpect(jsonPath("$.endpoints").isArray())
        .andExpect(jsonPath("$.endpoints").isNotEmpty())
        .andExpect(
            jsonPath("$.endpoints[0].uri").value("https://storm-tape.example.org:8443/api/v1"))
        .andExpect(jsonPath("$.endpoints[0].version").value("v1"))
        .andExpect(jsonPath("$.endpoints[0].metadata").isMap())
        .andExpect(jsonPath("$.endpoints[0].metadata['test']").exists())
        .andExpect(jsonPath("$.endpoints[0].metadata['test']").value("test"));
  }
}
