// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

// ForwardedHeaderFilter does not parse the "by" part of the Forwarded header
// This wraps the request so the local address/port are correct
// https://github.com/spring-projects/spring-framework/issues/34654
public class ForwardedByHeaderFilter extends OncePerRequestFilter {

  public static final Logger LOG = LoggerFactory.getLogger(ForwardedByHeaderFilter.class);

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    try {
      ServletRequest wrappedRequest = new ForwardedByHeaderRequest(request);
      filterChain.doFilter(wrappedRequest, response);
    } catch (Throwable e) {
      LOG.warn("Failed to apply forwarded header: {}", request.getHeader("Forwarded"), e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  protected String formatRequest(HttpServletRequest request) {
    return "HTTP " + request.getMethod() + " \"" + request.getRequestURI() + "\"";
  }

  // This is inspired by ForwardedHeaderExtractingRequest subclass of Spring Framework's
  // ForwardedHeaderFilter
  private static class ForwardedByHeaderRequest extends HttpServletRequestWrapper {

    private static final String FORWARDED_VALUE = "\"?([^;,\"]+)\"?";
    private static final Pattern FORWARDED_BY_PATTERN =
        Pattern.compile("(?i:by)=" + FORWARDED_VALUE);
    private final Optional<InetSocketAddress> localAddress;

    public ForwardedByHeaderRequest(HttpServletRequest servletRequest) {
      super(servletRequest);
      ServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
      HttpHeaders headers = request.getHeaders();
      this.localAddress = parseForwardedBy(headers);
    }

    // This is adapted from Spring Framework's ForwardedHeaderUtils
    private static Optional<InetSocketAddress> parseForwardedBy(HttpHeaders headers) {
      String forwardedHeader = headers.getFirst("Forwarded");
      if (!StringUtils.hasText(forwardedHeader)) {
        return Optional.empty();
      }
      String forwardedToUse = StringUtils.tokenizeToStringArray(forwardedHeader, ",")[0];
      Matcher matcher = FORWARDED_BY_PATTERN.matcher(forwardedToUse);
      if (matcher.find()) {
        String value = matcher.group(1).trim();
        int portSeparatorIdx = value.lastIndexOf(':');
        int squareBracketIdx = value.lastIndexOf(']');
        if (portSeparatorIdx > squareBracketIdx) {
          if (squareBracketIdx == -1 && value.indexOf(':') != portSeparatorIdx) {
            throw new IllegalArgumentException("Invalid IPv4 address: " + value);
          }
          String host = value.substring(0, portSeparatorIdx);
          try {
            int port = Integer.parseInt(value, portSeparatorIdx + 1, value.length(), 10);
            return Optional.of(InetSocketAddress.createUnresolved(host, port));
          } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                "Failed to parse a port from \"forwarded\"-type header value: " + value);
          }
        }
      }
      return Optional.empty();
    }

    @Override
    public String getLocalAddr() {
      return this.localAddress.map(InetSocketAddress::getHostString).orElse(super.getLocalAddr());
    }

    @Override
    public int getLocalPort() {
      return this.localAddress.map(InetSocketAddress::getPort).orElse(super.getLocalPort());
    }
  }
}
