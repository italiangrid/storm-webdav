package org.italiangrid.storm.webdav.test.tpc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransferRequestValidationTest extends TransferFilterTestSupport {
  
  @Before
  public void setup() {
    super.setup();
    when(request.getServletPath()).thenReturn(SERVLET_PATH);
    when(request.getPathInfo()).thenReturn(LOCAL_PATH);
    when(request.getMethod()).thenReturn(COPY.name());
    when(resolver.pathExists(FULL_LOCAL_PATH)).thenReturn(false);
    when(resolver.pathExists(FULL_LOCAL_PATH_PARENT)).thenReturn(true);
    
  }


  @Test
  public void sourceAndDestHeaderPresent() throws IOException, ServletException {
    when(request.getHeader(SOURCE_HEADER)).thenReturn(HTTP_URL);
    when(request.getHeader(DESTINATION_HEADER)).thenReturn(HTTP_URL);

    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
    assertThat(error.getValue(), containsString("both present"));
  }


  @Test
  public void invalidDestinationURIs() throws IOException, ServletException {
    for (String u : INVALID_URLs) {
      when(request.getHeader(DESTINATION_HEADER)).thenReturn(u);
      filter.doFilter(request, response, chain);
      verify(response).sendError(httpStatus.capture(), error.capture());
      assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
      assertThat(error.getValue(), containsString("Invalid Destination header"));
      reset(response);
    }
  }


  @Test
  public void invalidSourceURIs() throws IOException, ServletException {
    for (String u : INVALID_URLs) {
      when(request.getHeader(SOURCE_HEADER)).thenReturn(u);
      filter.doFilter(request, response, chain);
      verify(response).sendError(httpStatus.capture(), error.capture());
      assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
      assertThat(error.getValue(), containsString("Invalid Source header"));
      reset(response);
    }
  }

  @Test
  public void invalidOverwriteHeader() throws IOException, ServletException {

    String[] invalidValues = {"", "cccc", "whatever", "true", "false"};

    when(request.getHeader(SOURCE_HEADER)).thenReturn(HTTP_URL);

    for (String s : invalidValues) {
      when(request.getHeader(OVERWRITE_HEADER)).thenReturn(s);
      filter.doFilter(request, response, chain);
      verify(response).sendError(httpStatus.capture(), error.capture());
      assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
      assertThat(error.getValue(), containsString("Invalid Overwrite header"));
      reset(response);
    }
  }

  @Test
  public void invalidRequireChecksumHeader() throws IOException, ServletException {
    String[] invalidValues = {"t", "F", ""};

    when(request.getHeader(SOURCE_HEADER)).thenReturn(HTTP_URL);
    for (String s : invalidValues) {
      when(request.getHeader(REQUIRE_CHECKSUM_HEADER)).thenReturn(s);
      filter.doFilter(request, response, chain);
      verify(response).sendError(httpStatus.capture(), error.capture());
      assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
      assertThat(error.getValue(), containsString("Invalid RequireChecksumVerification header"));
      reset(response);
    }
  }

  @Test
  public void emptyOrNullPathInfo() throws IOException, ServletException {
    when(request.getHeader(SOURCE_HEADER)).thenReturn(HTTP_URL);

    String[] invalidPathInfos = {null, "", "does/not/start/with/slash"};
    String[] expectedErrorMsgs = {"Null or empty", "Null or empty", "Invalid local path"};
    
    for (int i=0; i < invalidPathInfos.length; i++) {
      when(request.getPathInfo()).thenReturn(invalidPathInfos[i]);

      filter.doFilter(request, response, chain);
      verify(response).sendError(httpStatus.capture(), error.capture());
      assertThat(httpStatus.getValue(), is(BAD_REQUEST.value()));
      assertThat(error.getValue(), containsString(expectedErrorMsgs[i]));
      reset(response);

    }

  }
  
  public void unresolvedStorageArea() throws IOException, ServletException {
    when(resolver.resolvePath(LOCAL_PATH)).thenReturn(null);
    filter.doFilter(request, response, chain);
    verify(response).sendError(httpStatus.capture(), error.capture());
    assertThat(httpStatus.getValue(), is(NOT_FOUND.value()));
    assertThat(error.getValue(), containsString("Invalid local path"));
  }

}
