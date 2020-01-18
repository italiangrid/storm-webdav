/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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

import java.net.URI;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.italiangrid.storm.webdav.tpc.StaticHostListLocalURLService;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.storm.webdav.tpc.TransferFilter;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
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
  public static final URI HTTPS_URL_URI = URI.create(HTTPS_URL);

  public static String[] INVALID_URLs =
      {"http:whatever", "httpg://storm.example/test", "gsiftp://whatever/test"};

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
  ArgumentCaptor<GetTransferRequest> getXferRequest;

  @Captor
  ArgumentCaptor<PutTransferRequest> putXferRequest;

  LocalURLService lus = new StaticHostListLocalURLService(Arrays.asList("localhost"));

  protected void setup() {
    filter = new TransferFilter(client, resolver, lus, true);
  }

}
