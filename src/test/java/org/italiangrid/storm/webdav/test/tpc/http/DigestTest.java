// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc.http;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.tpc.utils.Adler32DigestHeaderHelper.extractAdler32DigestFromResponse;
import static org.mockito.Mockito.lenient;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.italiangrid.storm.webdav.tpc.utils.Adler32DigestHeaderHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DigestTest {

  @Mock
  HttpResponse response;

  @Mock
  Header header;

  public static final String[] INVALID_HEADERS =
      {"", "adler54=1233456", "adler32=8a23d4f889", "sha256:437648", null};

  public static final String[] VALID_HEADERS =
      {"adler32=8a23d4f8", "adler32 =8a23d4f8", "adler32 =   8a23d4f8", "  adler32=8a23d4f8  "};


  protected void instrumentResponse(String headerValue) {
    lenient().when(response.getFirstHeader(Adler32DigestHeaderHelper.DIGEST_HEADER))
      .thenReturn(header);
    lenient().when(header.getName()).thenReturn(Adler32DigestHeaderHelper.DIGEST_HEADER);
    lenient().when(header.getValue()).thenReturn(headerValue);
  }

  @Test
  void testInvalidHeader() {

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
