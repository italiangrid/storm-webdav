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
package org.italiangrid.storm.webdav.tpc;

import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequestBuilder;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequestBuilder;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.italiangrid.storm.webdav.tpc.transfer.error.TransferError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferFilter extends TransferFilterSupport implements Filter {

  public static final Logger LOG = LoggerFactory.getLogger(TransferFilter.class);

  final TransferClient client;

  public TransferFilter(TransferClient c, PathResolver resolver, LocalURLService lus,
      boolean verifyChecksum) {
    super(resolver, lus, verifyChecksum);
    client = c;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    if (isTpc(req)) {
      handleCopy(req, res);
    } else {
      chain.doFilter(request, response);
    }
  }

  protected void handleCopy(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    if (validRequest(request, response)) {
      Optional<String> source = Optional.ofNullable(request.getHeader(SOURCE_HEADER));
      if (source.isPresent()) {
        handlePullCopy(request, response);
      } else {
        handlePushCopy(request, response);
      }
    }
  }

  @Override
  public void destroy() {


  }


  protected void reportProgress(TransferStatus s, HttpServletResponse r) {
    try {
      r.getWriter().write(s.asPerfMarker());
      r.getWriter().flush();
    } catch (IOException e) {
      LOG.warn("I/O error writing perf marker: {}. Swallowing it", e.getMessage(), e);
    }
  }

  protected void handlePullCopy(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    URI uri = URI.create(request.getHeader(SOURCE_HEADER));
    String path = getScopedPathInfo(request);

    GetTransferRequest xferRequest = GetTransferRequestBuilder.create()
      .uri(uri)
      .path(path)
      .headers(getTransferHeaders(request, response))
      .verifyChecksum(verifyChecksum && verifyChecksumRequested(request))
      .overwrite(overwriteRequested(request))
      .build();

    try {

      response.setStatus(SC_ACCEPTED);
      client.handle(xferRequest, s -> reportProgress(s, response));

    } catch (ChecksumVerificationError e) {
      handleChecksumVerificationError(e, response);
    } catch (TransferError e) {
      handleTransferError(e, response);
    } catch (HttpResponseException e) {
      handleHttpResponseException(e, response);
    } catch (ClientProtocolException e) {
      handleClientProtocolException(e, response);
    }
  }

  protected void handlePushCopy(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    URI uri = URI.create(request.getHeader(DESTINATION_HEADER));
    String path = getScopedPathInfo(request);

    PutTransferRequest xferRequest = PutTransferRequestBuilder.create()
      .uri(uri)
      .path(path)
      .headers(getTransferHeaders(request, response))
      .verifyChecksum(verifyChecksum && verifyChecksumRequested(request))
      .overwrite(overwriteRequested(request))
      .build();

    try {

      response.setStatus(SC_ACCEPTED);
      client.handle(xferRequest, s -> reportProgress(s, response));

    } catch (ChecksumVerificationError e) {
      handleChecksumVerificationError(e, response);
    } catch (TransferError e) {
      handleTransferError(e, response);
    } catch (HttpResponseException e) {
      handleHttpResponseException(e, response);
    } catch (ClientProtocolException e) {
      handleClientProtocolException(e, response);
    }
  }

}
