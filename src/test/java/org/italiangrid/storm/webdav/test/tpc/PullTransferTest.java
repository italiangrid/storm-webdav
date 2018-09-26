package org.italiangrid.storm.webdav.test.tpc;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.enumeration;
import static org.hamcrest.Matchers.is;
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Multimap;

@RunWith(MockitoJUnitRunner.class)
public class PullTransferTest extends TransferFilterTestSupport {

  
  @Before
  public void setup() {
    super.setup();
    when(request.getMethod()).thenReturn(COPY.name());
    when(request.getServletPath()).thenReturn(SERVLET_PATH);
    when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    when(request.getHeader(SOURCE_HEADER)).thenReturn(HTTP_URL);
    when(request.getHeaderNames()).thenReturn(emptyEnumeration());
    when(resolver.pathExists(FULL_LOCAL_PATH)).thenReturn(false);
    when(resolver.pathExists(FULL_LOCAL_PATH_PARENT)).thenReturn(true);
  }

  @Test
  public void pullEmptyTransferHeaders() throws IOException, ServletException {
    filter.doFilter(request, response, chain);
    verify(client).handle(xferRequest.capture(), Mockito.any());
    assertThat(xferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(xferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat(xferRequest.getValue().overwrite(), is(true));
    assertThat(xferRequest.getValue().verifyChecksum(), is(true));
    assertTrue("Expected empty xfer headers", xferRequest.getValue().transferHeaders().isEmpty());
  }

  @Test
  public void overwriteHeaderRecognized() throws IOException, ServletException {
    when(request.getHeader(OVERWRITE_HEADER)).thenReturn("F");
    filter.doFilter(request, response, chain);
    verify(client).handle(xferRequest.capture(), Mockito.any());
    assertThat(xferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(xferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat("Overwrite header not recognized", xferRequest.getValue().overwrite(), is(false));
    assertThat(xferRequest.getValue().verifyChecksum(), is(true));
    assertTrue("Expected empty xfer headers", xferRequest.getValue().transferHeaders().isEmpty());
  }

  @Test
  public void checksumRecognized() throws IOException, ServletException {
    when(request.getHeader(REQUIRE_CHECKSUM_HEADER)).thenReturn("false");
    filter.doFilter(request, response, chain);
    verify(client).handle(xferRequest.capture(), Mockito.any());
    assertThat(xferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(xferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat(xferRequest.getValue().overwrite(), is(true));
    assertThat("RequireChecksumVerification header not recognized",
        xferRequest.getValue().verifyChecksum(), is(false));
    assertTrue("Expected empty xfer headers", xferRequest.getValue().transferHeaders().isEmpty());
  }

  @Test
  public void checkTransferHeaderPassing() throws IOException, ServletException {
    when(request.getHeader(TRANSFER_HEADER_AUTHORIZATION_KEY))
      .thenReturn(TRANSFER_HEADER_AUTHORIZATION_VALUE);
    when(request.getHeader(TRANSFER_HEADER_WHATEVER_KEY))
      .thenReturn(TRANSFER_HEADER_WHATEVER_VALUE);

    when(request.getHeaderNames()).thenReturn(
        enumeration(asList(TRANSFER_HEADER_AUTHORIZATION_KEY, TRANSFER_HEADER_WHATEVER_KEY)));

    filter.doFilter(request, response, chain);
    verify(client).handle(xferRequest.capture(), Mockito.any());

    assertThat(xferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(xferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat(xferRequest.getValue().overwrite(), is(true));
    assertThat(xferRequest.getValue().verifyChecksum(), is(true));


    Multimap<String, String> xferHeaders = xferRequest.getValue().transferHeaders();
    assertThat(xferHeaders.size(), is(2));
    assertThat(xferHeaders.containsKey("Authorization"), is(true));
    assertThat(xferHeaders.get("Authorization").iterator().next(),
        is(TRANSFER_HEADER_AUTHORIZATION_VALUE));
    assertThat(xferHeaders.containsKey("Whatever"), is(true));
    assertThat(xferHeaders.get("Whatever").iterator().next(), is(TRANSFER_HEADER_WHATEVER_VALUE));
  }
  
  @Test
  public void emptyTransferHeaderAreIgnored() throws IOException, ServletException {
    when(request.getHeaderNames()).thenReturn(
        enumeration(asList(TRANSFER_HEADER, TRANSFER_HEADER_WHATEVER_KEY)));
    
    when(request.getHeader(TRANSFER_HEADER_WHATEVER_KEY))
      .thenReturn(TRANSFER_HEADER_WHATEVER_VALUE);
    
    filter.doFilter(request, response, chain);
    verify(client).handle(xferRequest.capture(), Mockito.any());

    assertThat(xferRequest.getValue().path(), is(FULL_LOCAL_PATH));
    assertThat(xferRequest.getValue().remoteURI(), is(HTTP_URL_URI));
    assertThat(xferRequest.getValue().overwrite(), is(true));
    assertThat(xferRequest.getValue().verifyChecksum(), is(true));


    Multimap<String, String> xferHeaders = xferRequest.getValue().transferHeaders();
    assertThat(xferHeaders.size(), is(1));
    
    assertThat(xferHeaders.containsKey("Whatever"), is(true));
    assertThat(xferHeaders.get("Whatever").iterator().next(), is(TRANSFER_HEADER_WHATEVER_VALUE));
  }
  
}
