// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Add 'Server' header response, using StoRM-WebDAV version (taken from the .jar file) and an
 * instance ID that is (with high likelihood) different between any two invocations of StoRM-WebDAV
 * instances.
 */
public class ServerResponseHeaderFilter extends OncePerRequestFilter {

  private static final int INSTANCE_ID_LENGTH = 4;

  private static final String SERVER_HEADER_VALUE =
      "StoRM-WebDAV/" + lookupStormVersion() + " (instance=" + calculateInstanceIdentifier() + ")";

  private static String lookupStormVersion() {
    return Optional.ofNullable(
            ServerResponseHeaderFilter.class.getPackage().getImplementationVersion())
        .orElse("unknown-version");
  }

  private static String calculateInstanceIdentifier() {
    byte[] in = ByteBuffer.allocate(Long.BYTES).putLong(System.currentTimeMillis()).array();
    byte[] encodedBytes = Base64.getEncoder().withoutPadding().encode(in);
    String encoded = new String(encodedBytes);
    return encoded.substring(encoded.length() - INSTANCE_ID_LENGTH);
  }

  @Override
  public void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    response.setHeader(HttpHeaders.SERVER, SERVER_HEADER_VALUE);
    NoDuplicateServerHeaderResponse wrappedResponse = new NoDuplicateServerHeaderResponse(response);

    chain.doFilter(request, wrappedResponse);
  }

  private static class NoDuplicateServerHeaderResponse extends HttpServletResponseWrapper {
    NoDuplicateServerHeaderResponse(HttpServletResponse response) {
      super(response);
    }

    @Override
    public void addHeader(String name, String value) {
      if (!name.equals(HttpHeaders.SERVER)) {
        super.addHeader(name, value);
      }
    }
  }
}
