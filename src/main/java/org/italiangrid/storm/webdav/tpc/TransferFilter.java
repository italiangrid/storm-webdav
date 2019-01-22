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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.server.tracing.RequestIdHolder;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequestBuilder;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequestBuilder;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.TransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.italiangrid.storm.webdav.tpc.transfer.error.TransferError;
import org.italiangrid.storm.webdav.tpc.utils.ClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class TransferFilter extends TransferFilterSupport implements Filter {

  public static final String XFER_ID_KEY = "tpc.xferId";

  public static final Logger LOG = LoggerFactory.getLogger(TransferFilter.class);

  final TransferClient client;

  public TransferFilter(TransferClient c, PathResolver resolver, LocalURLService lus,
      boolean verifyChecksum) {
    super(resolver, lus, verifyChecksum);
    client = c;
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

    setupLoggingContext(request);

    try {

      if (validRequest(request, response)) {
        Optional<String> source = Optional.ofNullable(request.getHeader(SOURCE_HEADER));
        if (source.isPresent()) {
          handlePullCopy(request, response);
        } else {
          handlePushCopy(request, response);
        }
      }

    } finally {
      MDC.clear();
    }
  }

  private void setupLoggingContext(HttpServletRequest request) {
    Optional<String> clientInfoHeader = Optional.ofNullable(request.getHeader(CLIENT_INFO_HEADER));

    if (clientInfoHeader.isPresent()) {
      try {
        ClientInfo.fromHeaderString(clientInfoHeader.get()).addToMDC();
      } catch(IllegalArgumentException e) {
        LOG.warn("Error parsing ClientInfo header: {}", clientInfoHeader.get());
      }
    }
  }

  @Override
  public void destroy() {


  }


  protected void reportProgress(TransferRequest request, TransferStatus s, HttpServletResponse r) {
    try {
      r.getWriter().write(s.asPerfMarker());
      r.getWriter().flush();
    } catch (IOException e) {
      LOG.warn("I/O error writing perf marker: {}. Swallowing it", e.getMessage(), e);
    }
  }

  protected void logTransferStart(GetTransferRequest req) {
    LOG.info("Pull third-party transfer requested: Source: {}, Destination: {}", req.remoteURI(),
        req.path());
    if (LOG.isDebugEnabled()) {
      LOG.debug("{}", req);
    }
  }

  protected void logTransferStart(PutTransferRequest req) {
    LOG.info("Push third-party transfer requested: Source: {}, Destination: {}", req.path(),
        req.remoteURI());
    if (LOG.isDebugEnabled()) {
      LOG.debug("{}", req);
    }
  }

  protected void logTransferDone(GetTransferRequest req) {
    if (req.lastTransferStatus().isPresent() && LOG.isInfoEnabled()) {
      LOG.info("Pull third-party transfer completed: {}. Source: {}, Destination: {}",
          req.lastTransferStatus().get(), req.remoteURI(), req.path());
    }
  }

  protected void logTransferDone(PutTransferRequest req) {
    if (req.lastTransferStatus().isPresent() && LOG.isInfoEnabled()) {
      LOG.info("Push third-party transfer completed: {}. Source: {}, Destination: {}",
          req.lastTransferStatus().get(), req.path(), req.remoteURI());
    }
  }

  protected void handlePullCopy(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    URI uri = URI.create(request.getHeader(SOURCE_HEADER));
    String path = getScopedPathInfo(request);

    GetTransferRequest xferRequest = GetTransferRequestBuilder.create()
      .uuid(RequestIdHolder.getRequestId())
      .uri(uri)
      .path(path)
      .headers(getTransferHeaders(request, response))
      .verifyChecksum(verifyChecksum && verifyChecksumRequested(request))
      .overwrite(overwriteRequested(request))
      .build();

    logTransferStart(xferRequest);

    try {

      response.setStatus(SC_ACCEPTED);
      client.handle(xferRequest, (r, s) -> reportProgress(xferRequest, s, response));

    } catch (ChecksumVerificationError e) {
      handleChecksumVerificationError(xferRequest, e, response);
    } catch (TransferError e) {
      handleTransferError(xferRequest, e, response);
    } catch (HttpResponseException e) {
      handleHttpResponseException(xferRequest, e, response);
    } catch (ClientProtocolException e) {
      handleClientProtocolException(xferRequest, e, response);
    } finally {
      logTransferDone(xferRequest);
    }
  }

  protected void handlePushCopy(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    URI uri = URI.create(request.getHeader(DESTINATION_HEADER));
    String path = getScopedPathInfo(request);

    PutTransferRequest xferRequest = PutTransferRequestBuilder.create()
      .uuid(RequestIdHolder.getRequestId())
      .uri(uri)
      .path(path)
      .headers(getTransferHeaders(request, response))
      .verifyChecksum(verifyChecksum && verifyChecksumRequested(request))
      .overwrite(overwriteRequested(request))
      .build();

    MDC.put(XFER_ID_KEY, xferRequest.uuid());
    logTransferStart(xferRequest);

    try {

      response.setStatus(SC_ACCEPTED);
      client.handle(xferRequest, (r, s) -> reportProgress(xferRequest, s, response));

    } catch (ChecksumVerificationError e) {
      handleChecksumVerificationError(xferRequest, e, response);
    } catch (TransferError e) {
      handleTransferError(xferRequest, e, response);
    } catch (HttpResponseException e) {
      handleHttpResponseException(xferRequest, e, response);
    } catch (ClientProtocolException e) {
      handleClientProtocolException(xferRequest, e, response);
    } finally {
      logTransferDone(xferRequest);
    }
  }

}
