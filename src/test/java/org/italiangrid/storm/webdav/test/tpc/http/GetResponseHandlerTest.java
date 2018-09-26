package org.italiangrid.storm.webdav.test.tpc.http;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.tpc.http.GetResponseHandler;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatusCallback;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetResponseHandlerTest extends ClientTestSupport {

  @Mock
  StatusLine status;
  
  @Mock
  HttpEntity entity;
  
  @Mock
  HttpResponse response;
  
  @Mock
  TransferStatusCallback cb;
  
  @Mock
  StormCountingOutputStream os;
  
  @Mock
  ExtendedAttributesHelper ah;
  
  GetResponseHandler handler;

  @Before
  public void setup() {
    
    handler = new GetResponseHandler(false, os, cb, ah);
    when(response.getStatusLine()).thenReturn(status);
    when(response.getEntity()).thenReturn(entity);
  }
  
  @Test
  public void handlerWritesToStream() throws IOException {
    when(status.getStatusCode()).thenReturn(200);
    
    handler.handleResponse(response);
    
    verify(entity).writeTo(any());
    verify(ah).setChecksumAttribute(ArgumentMatchers.<Path>any(), any());
  }
}
