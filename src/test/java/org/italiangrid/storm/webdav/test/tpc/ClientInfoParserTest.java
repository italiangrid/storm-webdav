// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.italiangrid.storm.webdav.tpc.utils.ClientInfo;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
class ClientInfoParserTest {

  @Test
  void testClientInfoHeaderParsing() {
    ClientInfo ci = ClientInfo
      .fromHeaderString("job-id=34f98a5e-1e49-11e9-ab17-fa163edecedf;file-id=8764139989;retry=0");

    assertThat(ci.getJobId(), is("34f98a5e-1e49-11e9-ab17-fa163edecedf"));
    assertThat(ci.getFileId(), is("8764139989"));
    assertThat(ci.getRetryCount(), is(0));
  }

}
