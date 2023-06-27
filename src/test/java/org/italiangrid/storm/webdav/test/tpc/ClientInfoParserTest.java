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
package org.italiangrid.storm.webdav.test.tpc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.italiangrid.storm.webdav.tpc.utils.ClientInfo;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ClientInfoParserTest {

  @Test
  public void testClientInfoHeaderParsing() {
    ClientInfo ci = ClientInfo
      .fromHeaderString("job-id=34f98a5e-1e49-11e9-ab17-fa163edecedf;file-id=8764139989;retry=0");

    assertThat(ci.getJobId(), is("34f98a5e-1e49-11e9-ab17-fa163edecedf"));
    assertThat(ci.getFileId(), is("8764139989"));
    assertThat(ci.getRetryCount(), is(0));
  }

}
