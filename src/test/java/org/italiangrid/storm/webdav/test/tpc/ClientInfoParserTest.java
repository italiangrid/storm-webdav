// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.italiangrid.storm.webdav.tpc.utils.ClientInfo;
import org.junit.jupiter.api.Test;

class ClientInfoParserTest {

  @Test
  void testClientInfoHeaderParsing() {
    ClientInfo ci =
        ClientInfo.fromHeaderString(
            "job-id=34f98a5e-1e49-11e9-ab17-fa163edecedf;file-id=8764139989;retry=0");

    assertThat(ci.jobId(), is("34f98a5e-1e49-11e9-ab17-fa163edecedf"));
    assertThat(ci.fileId(), is("8764139989"));
    assertThat(ci.retryCount(), is(0));
  }
}
