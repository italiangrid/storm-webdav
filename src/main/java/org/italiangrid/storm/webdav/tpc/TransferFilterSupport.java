// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc;

import static java.lang.String.format;
import static jakarta.servlet.http.HttpServletResponse.SC_PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.Enumeration;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.HeaderElements;
import org.apache.hc.core5.http.HttpHeaders;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.transfer.TransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.italiangrid.storm.webdav.tpc.transfer.error.TransferError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class TransferFilterSupport implements TpcUtils {

  public static final Logger LOG = LoggerFactory.getLogger(TransferFilterSupport.class);

  protected final Clock clock;
  protected final PathResolver resolver;
  protected final LocalURLService localURLService;
  protected final boolean verifyChecksum;
  protected final TransferStatus.Builder status;
  protected long enableExpectContinueThreshold;


  protected TransferFilterSupport(Clock clock, PathResolver resolver, LocalURLService lus,
      boolean verifyChecksum, long enableExpectContinueThreshold) {
    this.clock = clock;
    this.resolver = resolver;
    this.localURLService = lus;
    this.verifyChecksum = verifyChecksum;
    this.enableExpectContinueThreshold = enableExpectContinueThreshold;
    status = TransferStatus.builder(clock);
  }

  protected String getScopedPathInfo(HttpServletRequest request) {
    return Paths.get(request.getServletPath(), request.getPathInfo()).toString();
  }

  protected Multimap<String, String> getTransferHeaders(HttpServletRequest request,
      HttpServletResponse response) {

    Multimap<String, String> xferHeaders = ArrayListMultimap.create();
    Enumeration<String> headerNames = request.getHeaderNames();

    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();

      if (headerName.toLowerCase().startsWith(TransferConstants.TRANSFER_HEADER_LC)) {
        String xferHeaderName = headerName.substring(TransferConstants.TRANFER_HEADER_LENGTH);
        if (xferHeaderName.isBlank()) {
          LOG.warn("Ignoring invalid transfer header {}", headerName);
          continue;
        }
        if (xferHeaderName.trim().equalsIgnoreCase(SciTag.SCITAG_HEADER)
            && request.getHeader(SciTag.SCITAG_HEADER) != null) {
          // If the active party receives an HTTP-TPC COPY request with both a SciTag request header
          // and a TransferHeaderSciTag request header then it SHOULD ignore the
          // TransferHeaderSciTag and continue to process the request.
          LOG.warn("Ignoring TransferHeaderSciTag header because SciTag header is present");
          continue;
        }
        xferHeaders.put(xferHeaderName.trim(), request.getHeader(headerName));
      }
    }

    if (isPushTpc(request, localURLService)
        && request.getContentLength() >= enableExpectContinueThreshold) {
      xferHeaders.put(HttpHeaders.EXPECT, HeaderElements.CONTINUE);
    }

    return xferHeaders;
  }

  protected boolean verifyChecksumRequested(HttpServletRequest request) {
    Optional<String> verifyChecksumFromHeader =
        Optional.ofNullable(request.getHeader(TransferConstants.REQUIRE_CHECKSUM_HEADER));

    if (verifyChecksumFromHeader.isPresent()) {
      return "true".equals(verifyChecksumFromHeader.get());
    }
    // FIXME: take default from configuration
    return true;
  }

  protected boolean overwriteRequested(HttpServletRequest request) {
    Optional<String> overwrite =
        Optional.ofNullable(request.getHeader(TransferConstants.OVERWRITE_HEADER));

    if (overwrite.isPresent()) {
      return "T".equalsIgnoreCase(overwrite.get());
    }

    return true;
  }


  protected boolean isSupportedTransferURI(URI uri) {
    return TransferConstants.SUPPORTED_PROTOCOLS.contains(uri.getScheme()) && uri.getPath() != null;
  }

  protected boolean validTransferURI(String xferUri) {

    boolean result = false;

    try {
      URI uri = new URI(xferUri);

      if (!isSupportedTransferURI(uri)) {
        LOG.warn("Unsupported transfer URI: {}", uri);
        result = false;
      } else {
        result = true;
      }



    } catch (URISyntaxException e) {
      LOG.warn("Error parsing transfer URI: {}", e.getMessage());
      result = false;
    }

    return result;

  }

  protected void conflict(HttpServletResponse response, String msg) throws IOException {
    response.sendError(HttpStatus.CONFLICT.value(), msg);
  }

  protected void preconditionFailed(HttpServletResponse response, String msg) throws IOException {
    response.sendError(HttpStatus.PRECONDITION_FAILED.value(), msg);
  }

  protected void notFound(HttpServletResponse response, String msg) throws IOException {
    LOG.info("Not found: {}", msg);
    response.sendError(HttpStatus.NOT_FOUND.value(), msg);
  }

  protected void invalidRequest(HttpServletResponse response, String msg) throws IOException {
    LOG.info("Invalid request: {}", msg);
    response.sendError(BAD_REQUEST.value(), msg);
  }


  protected boolean validLocalSourcePath(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String servletPath = request.getServletPath();

    Optional<String> pathInfo = Optional.ofNullable(request.getPathInfo());

    if (!pathInfo.isPresent() || pathInfo.get().isBlank()) {
      invalidRequest(response, "Null or empty local path information!");
      return false;
    }

    if (!pathInfo.get().startsWith("/")) {
      invalidRequest(response, "Invalid local path: " + pathInfo.get());
      return false;
    }

    Path localPath = Paths.get(servletPath, pathInfo.get());

    if (!resolver.pathExists(localPath.toString())) {
      notFound(response, "Local source path not found: " + localPath.toString());
      return false;
    }

    return true;
  }



  protected boolean validLocalDestinationPath(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    String servletPath = request.getServletPath();

    Optional<String> pathInfo = Optional.ofNullable(request.getPathInfo());

    if (!pathInfo.isPresent() || pathInfo.get().isBlank()) {
      invalidRequest(response, "Null or empty local path information!");
      return false;
    }

    if (!pathInfo.get().startsWith("/")) {
      invalidRequest(response, "Invalid local path: " + pathInfo.get());
      return false;
    }

    Path localPath = Paths.get(servletPath, pathInfo.get());
    final boolean overwriteRequested = overwriteRequested(request);

    if (!overwriteRequested && resolver.pathExists(localPath.toString())) {
      preconditionFailed(response, "Target file exists and Overwrite is false");
      return false;
    }

    String parentPath = localPath.getParent().toString();
    if (!resolver.pathExists(parentPath)) {
      conflict(response, "Parent resource does not exist");
      return false;
    }

    return true;
  }

  protected boolean validRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Optional<String> source =
        Optional.ofNullable(request.getHeader(TransferConstants.SOURCE_HEADER));
    Optional<String> dest =
        Optional.ofNullable(request.getHeader(TransferConstants.DESTINATION_HEADER));
    Optional<String> overwrite =
        Optional.ofNullable(request.getHeader(TransferConstants.OVERWRITE_HEADER));
    Optional<String> checksum =
        Optional.ofNullable(request.getHeader(TransferConstants.REQUIRE_CHECKSUM_HEADER));
    Optional<String> credential =
        Optional.ofNullable(request.getHeader(TransferConstants.CREDENTIAL_HEADER));

    if (source.isPresent() && dest.isPresent()) {
      invalidRequest(response, "Source and Destination headers are both present!");
      return false;
    }

    if (source.isPresent()
        && !validTransferURI(request.getHeader(TransferConstants.SOURCE_HEADER))) {
      invalidRequest(response, format("Invalid %s header: %s", TransferConstants.SOURCE_HEADER,
          request.getHeader(TransferConstants.SOURCE_HEADER)));
      return false;
    }

    if (dest.isPresent()
        && !validTransferURI(request.getHeader(TransferConstants.DESTINATION_HEADER))) {
      invalidRequest(response, format("Invalid %s header: %s", TransferConstants.DESTINATION_HEADER,
          request.getHeader(TransferConstants.DESTINATION_HEADER)));
      return false;
    }

    if (source.isPresent() && !validLocalDestinationPath(request, response)) {
      return false;
    }

    if (dest.isPresent() && !validLocalSourcePath(request, response)) {
      return false;
    }

    if (overwrite.isPresent()) {
      boolean invalidOverwrite = false;

      String val = overwrite.get();

      if (val.isBlank() || val.trim().length() > 1
          || (!"T".equalsIgnoreCase(val) && !"F".equalsIgnoreCase(val))) {
        invalidOverwrite = true;
      }

      if (invalidOverwrite) {
        invalidRequest(response,
            format("Invalid %s header value: %s", TransferConstants.OVERWRITE_HEADER, val));
        return false;
      }
    }

    if (checksum.isPresent()) {

      boolean invalidChecksum = false;

      String val = checksum.get();

      if (val.isBlank() || (!"true".equals(val) && !"false".equals(val))) {
        invalidChecksum = true;
      }

      if (invalidChecksum) {
        invalidRequest(response,
            format("Invalid %s header value: %s", TransferConstants.REQUIRE_CHECKSUM_HEADER, val));
        return false;
      }
    }

    if (credential.isPresent()
        && !TransferConstants.CREDENTIAL_HEADER_NONE_VALUE.equals(credential.get())) {
      invalidRequest(response, "Unsupported Credential header value: " + credential.get());
      return false;
    }

    return true;
  }

  public void handleChecksumVerificationError(TransferRequest req, ChecksumVerificationError e,
      HttpServletResponse response) throws IOException {
    req.setTransferStatus(status.error(e.getMessage()));
    response.sendError(SC_PRECONDITION_FAILED, e.getMessage());

  }

  public void handleTransferError(TransferRequest req, TransferError e,
      HttpServletResponse response) throws IOException {
    req.setTransferStatus(status.error(e.getMessage()));
    response.sendError(SC_PRECONDITION_FAILED, e.getMessage());

  }

  public void handleClientProtocolException(TransferRequest req, ClientProtocolException e,
      HttpServletResponse response) throws IOException {
    req.setTransferStatus(status.error(e.getMessage()));
    response.sendError(SC_PRECONDITION_FAILED,
        format("Third party transfer error: %s", e.getMessage()));

  }

  public void handleHttpResponseException(TransferRequest req, HttpResponseException e,
      HttpServletResponse response) throws IOException {
    req.setTransferStatus(status.error(e.getMessage()));
    response.sendError(SC_PRECONDITION_FAILED,
        format("Third party transfer error: %d %s", e.getStatusCode(), e.getMessage()));

  }
}
