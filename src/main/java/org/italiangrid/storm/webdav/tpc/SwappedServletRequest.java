// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

// Wraps a request so that the Source header becomes the request URL and the request URL becomes the
// Destination header. This is necessary because the WebDAV protocol defines COPY only with the
// Destination header.
public class SwappedServletRequest extends HttpServletRequestWrapper {
  public static final Logger LOG = LoggerFactory.getLogger(SwappedServletRequest.class);

  final HttpServletRequest req;

  public SwappedServletRequest(HttpServletRequest req) {
    super(req);
    this.req = req;
  }

  // The getHeader method is used by milton to obtain information about the COPY request
  // https://github.com/miltonio/milton2/blob/master/milton-server-ce/src/main/java/io/milton/servlet/ServletRequest.java#L122
  @Override
  public String getHeader(String name) {
    if (TransferConstants.DESTINATION_HEADER.equals(name)) {
      // The Destination header is the requested URL of the original request
      return req.getRequestURL().toString();
    } else if (HttpHeaders.HOST.equals(name)) {
      // The Host header is the Host specified in the Source header of the
      // original request if it is present
      try {
        URI sourceUri = new URI(req.getHeader(TransferConstants.SOURCE_HEADER));
        String sourceHost = sourceUri.getHost();
        if (sourceHost != null) {
          return sourceHost + ':' + sourceUri.getPort();
        }
      } catch (URISyntaxException e) {
        LOG.warn("Error parsing Source header: {}", e.getMessage(), e);
      }
    }
    return req.getHeader(name);
  }

  // The getRequestURL method is used by milton to obtain information about the COPY request
  // https://github.com/miltonio/milton2/blob/master/milton-server-ce/src/main/java/io/milton/http/UrlAdapterImpl.java#L32
  @Override
  public StringBuffer getRequestURL() {
    String source = req.getHeader(TransferConstants.SOURCE_HEADER);
    try {
      // If the Source header of the original request includes the host, just
      // use it as the request URL
      String sourceHost = new URI(source).getHost();
      if (sourceHost != null) {
        return new StringBuffer(source);
      }
    } catch (URISyntaxException e) {
      LOG.warn("Error parsing Source header: {}", e.getMessage(), e);
    }
    // Otherwise get the scheme, host and port from the original request and use the Source header
    // as the path
    return new StringBuffer(
        req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + source);
  }
}
