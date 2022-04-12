/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.test.tpc.http;

import static org.hamcrest.CoreMatchers.is;
import static org.italiangrid.storm.webdav.tpc.utils.Adler32DigestHeaderHelper.extractAdler32DigestFromResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.italiangrid.storm.webdav.tpc.utils.Adler32DigestHeaderHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestTest {

  @Mock
  HttpResponse response;

  @Mock
  Header header;

  public static String[] INVALID_HEADERS =
      {"", "adler54=1233456", "adler32=8a23d4f889", "sha256:437648", null};

  public static final String[] VALID_HEADERS =
      {"adler32=8a23d4f8", "adler32 =8a23d4f8", "adler32 =   8a23d4f8", "  adler32=8a23d4f8  "};


  protected void instrumentResponse(String headerValue) {
    when(response.getFirstHeader(Adler32DigestHeaderHelper.DIGEST_HEADER)).thenReturn(header);
    when(header.getName()).thenReturn(Adler32DigestHeaderHelper.DIGEST_HEADER);
    when(header.getValue()).thenReturn(headerValue);
  }

  @Test
  public void testInvalidHeader() {

    for (String s : INVALID_HEADERS) {
      instrumentResponse(s);
      assertThat(extractAdler32DigestFromResponse(response).isPresent(), is(false));
    }

    for (String s : VALID_HEADERS) {
      instrumentResponse(s);
      assertThat(extractAdler32DigestFromResponse(response).isPresent(), is(true));
      assertThat(extractAdler32DigestFromResponse(response).get(), is("8a23d4f8"));
    }
  }
}
