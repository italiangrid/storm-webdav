// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tape;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@AutoConfigureMockMvc
@ActiveProfiles({"dev"})
@SpringBootTest(properties = { "storm.tape.well-known.source=not-a-file" })
@WithAnonymousUser
class DisabledEndpointTest {

  @Autowired
  MockMvc mvc;

  @Test
  void testDisabledWellKnown() throws Exception {
    mvc.perform(get("/.well-known/wlcg-tape-rest-api"))
      .andExpect(status().isNotFound());
  }
}
