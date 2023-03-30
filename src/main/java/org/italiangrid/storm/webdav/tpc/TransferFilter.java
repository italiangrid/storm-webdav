/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Clock;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.protocol.HTTP;
import org.italiangrid.storm.webdav.error.BadRequest;
import org.italiangrid.storm.webdav.error.ResourceNotFound;
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

import com.google.common.collect.Multimap;

public class TransferFilter extends TransferFilterSupport implements Filter {

  public static final String XFER_ID_KEY = "tpc.xferId";
  private static final String EMPTY_VALUE = "-";

  public static final Logger LOG = LoggerFactory.getLogger(TransferFilter.class);

  final TransferClient client;

  public TransferFilter(Clock clock, TransferClient c, PathResolver resolver, LocalURLService lus,
      boolean verifyChecksum, long enableExpectContinueThreshold) {
    super(clock, resolver, lus, verifyChecksum, enableExpectContinueThreshold);
    client = c;
  }

  private void localCopySanityChecks(HttpServletRequest req) throws MalformedURLException {
    if (!requestPathAndDestinationHeaderAreInSameStorageArea(req, resolver)) {
      throw new BadRequest("Local copy across storage areas is not supported");
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    if (isTpc(req, localURLService)) {
      handleTpc(req, res);
    } else if (isCopy(req) && requestHasLocalDestinationHeader(req, localURLService)) {
      try {
        localCopySanityChecks(req);
        // Let milton handle the local copy
        chain.doFilter(request, response);
      } catch (MalformedURLException | BadRequest | ResourceNotFound e) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.setContentType("text/plain");
        res.getWriter().print(e.getMessage());
        res.flushBuffer();
      }
    } else {
      chain.doFilter(request, response);
    }

  }

  protected void handleTpc(HttpServletRequest request, HttpServletResponse response)
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
      } catch (IllegalArgumentException e) {
        LOG.warn("Error parsing ClientInfo header: {}", clientInfoHeader.get());
      }
    }
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

    LOG.info(
        "Pull third-party transfer requested: Source: {}, Destination: {}, hasAuthorizationHeader: {}, id: {}",
        req.remoteURI(), req.path(), req.transferHeaders().containsKey(AUTHORIZATION_HEADER),
        req.uuid());
    if (LOG.isDebugEnabled()) {
      LOG.debug("{}", req);
    }
  }

  protected void logTransferStart(PutTransferRequest req) {
    LOG.info(
        "Push third-party transfer requested: Source: {}, Destination: {}, hasAuthorizationHeader: {}, id: {}",
        req.path(), req.remoteURI(), req.transferHeaders().containsKey(AUTHORIZATION_HEADER),
        req.uuid());
    if (LOG.isDebugEnabled()) {
      LOG.debug("{}", req);
    }
  }

  private String getUserFriendlyThroughputString(TransferRequest req) {
    String xferThroughputString = EMPTY_VALUE;

    if (req.transferThroughputBytesPerSec().isPresent()) {
      xferThroughputString =
          FileUtils.byteCountToDisplaySize(req.transferThroughputBytesPerSec().get().longValue());
    }

    return xferThroughputString;
  }

  protected void logTransferDone(GetTransferRequest req) {
    if (req.lastTransferStatus().isPresent() && LOG.isInfoEnabled()) {
      TransferStatus lastStatus = req.lastTransferStatus().get();
      if (TransferStatus.Status.DONE.equals(lastStatus.getStatus())) {
        LOG.info(
            "Pull third-party transfer completed: {}. Source: {}, Destination: {}, Bytes transferred: {}, Duration (msec): {}, Throughput: {}/sec, id: {}",
            lastStatus, req.remoteURI(), req.path(),
            req.bytesTransferred(),
            req.duration().toMillis(),
            getUserFriendlyThroughputString(req),
            req.uuid());
      } else {
        LOG.warn("Pull third-party transfer completed: {}. Source: {}, Destination: {}", lastStatus,
            req.remoteURI(), req.path());
      }
    }
  }

  protected void logTransferDone(PutTransferRequest req) {
    if (req.lastTransferStatus().isPresent() && LOG.isInfoEnabled()) {

      LOG.info(
          "Push third-party transfer completed: {}. Source: {}, Destination: {}, Bytes transferred: {}, Duration (msec): {}, Throughput: {}/sec, id: {}",
          req.lastTransferStatus().get(), req.path(), req.remoteURI(),
          req.bytesTransferred(), req.duration().toMillis(), getUserFriendlyThroughputString(req),
          req.uuid());
    }
  }

  protected void logTransferException(TransferRequest request, Exception e) {
    LOG.warn("Third-party transfer {} terminated with an error: {} - {}", request.uuid(),
        e.getClass().getName(), e.getMessage());
    if (LOG.isDebugEnabled()) {
      LOG.warn(e.getMessage(), e);
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
      logTransferException(xferRequest, e);
      handleChecksumVerificationError(xferRequest, e, response);
    } catch (TransferError e) {
      logTransferException(xferRequest, e);
      handleTransferError(xferRequest, e, response);
    } catch (HttpResponseException e) {
      logTransferException(xferRequest, e);
      handleHttpResponseException(xferRequest, e, response);
    } catch (ClientProtocolException e) {
      logTransferException(xferRequest, e);
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

    logTransferStart(xferRequest);

    try {

      response.setStatus(SC_ACCEPTED);
      client.handle(xferRequest, (r, s) -> reportProgress(xferRequest, s, response));

    } catch (ChecksumVerificationError e) {
      logTransferException(xferRequest, e);
      handleChecksumVerificationError(xferRequest, e, response);
    } catch (TransferError e) {
      logTransferException(xferRequest, e);
      handleTransferError(xferRequest, e, response);
    } catch (HttpResponseException e) {
      logTransferException(xferRequest, e);
      handleHttpResponseException(xferRequest, e, response);
    } catch (ClientProtocolException e) {
      logTransferException(xferRequest, e);
      handleClientProtocolException(xferRequest, e, response);
    } finally {
      logTransferDone(xferRequest);
    }
  }

}
