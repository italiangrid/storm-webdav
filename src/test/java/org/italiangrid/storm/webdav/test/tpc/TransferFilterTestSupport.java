package org.italiangrid.storm.webdav.test.tpc;

import java.net.URI;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.storm.webdav.tpc.TransferFilter;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public class TransferFilterTestSupport implements TransferConstants {
  
  public static final String SERVLET_PATH = "/test";
  public static final String LOCAL_PATH = "/some/file";
  public static final String LOCAL_PATH_PARENT = "/some";
  
  public static final String FULL_LOCAL_PATH = SERVLET_PATH + LOCAL_PATH;
  public static final String FULL_LOCAL_PATH_PARENT = SERVLET_PATH + LOCAL_PATH_PARENT;
  
  public static final String HTTP_URL = "http://storm.example/test/some/file";
  public static final String HTTPS_URL = "https://storm.example/test/some/file";
  public static final String DAV_URL = "dav://storm.example/test/some/file";
  public static final String DAVS_URL = "davs://storm.example/test/some/file";
  
  public static final String TRANSFER_HEADER = "TransferHeader";
  public static final String TRANSFER_HEADER_AUTHORIZATION_KEY = "TransferHeaderAuthorization";
  public static final String TRANSFER_HEADER_AUTHORIZATION_VALUE = "Bearer 123456";
  
  public static final String TRANSFER_HEADER_WHATEVER_KEY = "TransferHeaderWhatever";
  public static final String TRANSFER_HEADER_WHATEVER_VALUE = "papisilviobelluscona";
  
  public static final URI HTTP_URL_URI = URI.create(HTTP_URL);

  public static String[] INVALID_URLs = {"http:whatever", "httpg://storm.example/test", "gsiftp://whatever/test"};
  
  @Mock
  FilterChain chain;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  TransferClient client;

  @Mock
  PathResolver resolver;
  
  TransferFilter filter;

  @Captor
  ArgumentCaptor<String> error;

  @Captor
  ArgumentCaptor<Integer> httpStatus;

  @Captor
  ArgumentCaptor<GetTransferRequest> xferRequest;
  
  protected void setup() {
    filter = new TransferFilter(client, resolver, true);
  }
  public TransferFilterTestSupport() {
    // TODO Auto-generated constructor stub
  }

}
