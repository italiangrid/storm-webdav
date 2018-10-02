/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.ServletException;

import org.italiangrid.storm.webdav.server.servlet.WebDAVMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

@RunWith(MockitoJUnitRunner.class)
public class TransferFilterTest extends TransferFilterTestSupport {

  @Before
  public void setup() {
    super.setup();
  }

  @Test
  public void filterOnlyHandlesTpc() throws IOException, ServletException {

    // Ignore Http methods
    for (HttpMethod m : HttpMethod.values()) {
      when(request.getMethod()).thenReturn(m.toString());
      filter.doFilter(request, response, chain);
      verify(chain).doFilter(request, response);
      reset(chain);
    }

    // Ignore other WebDAV methods
    EnumSet<WebDAVMethod> nonCopyMethods = EnumSet.complementOf(EnumSet.of(WebDAVMethod.COPY));
    for (WebDAVMethod m : nonCopyMethods) {
      when(request.getMethod()).thenReturn(m.toString());
      filter.doFilter(request, response, chain);
      verify(chain).doFilter(request, response);
      reset(chain);
    }

    // No source or destination header
    when(request.getHeader(SOURCE_HEADER)).thenReturn(null);
    when(request.getHeader(DESTINATION_HEADER)).thenReturn(null);

    when(request.getMethod()).thenReturn(COPY.toString());
    filter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    reset(chain);

    // Local destination header
    when(request.getHeader(DESTINATION_HEADER)).thenReturn("/some/other/local/file");
    filter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    reset(chain);


    // Remote source header
    when(request.getHeader(SOURCE_HEADER)).thenReturn(HTTP_URL);
    filter.doFilter(request, response, chain);
    verifyZeroInteractions(chain);
    reset(chain);


    // Remote destination header
    when(request.getHeader(DESTINATION_HEADER)).thenReturn(HTTP_URL);
    filter.doFilter(request, response, chain);
    verifyZeroInteractions(chain);
    reset(chain);
  }


}
