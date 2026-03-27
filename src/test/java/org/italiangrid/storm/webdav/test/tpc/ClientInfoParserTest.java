// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.italiangrid.storm.webdav.tpc.utils.ClientInfo;
import org.junit.jupiter.api.Test;

class ClientInfoParserTest {

  @Test
  void testClientInfoHeaderParsing() {
    ClientInfo ci =
        ClientInfo.fromHeaderString(
            "job-id=34f98a5e-1e49-11e9-ab17-fa163edecedf;file-id=8764139989;retry=0");

    assertEquals("34f98a5e-1e49-11e9-ab17-fa163edecedf", ci.jobId());
    assertEquals("8764139989", ci.fileId());
    assertEquals(0, ci.retryCount());
  }
}
