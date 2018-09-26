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
    when(request.getServletPath()).thenReturn(SERVLET_PATH);
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
