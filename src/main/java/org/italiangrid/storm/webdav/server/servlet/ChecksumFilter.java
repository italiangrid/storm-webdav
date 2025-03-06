// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

import static java.lang.String.format;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class ChecksumFilter implements Filter {

  private final ExtendedAttributesHelper attributeHelper;
  private final PathResolver resolver;

  public static final Logger logger = LoggerFactory.getLogger(ChecksumFilter.class);

  @Autowired
  public ChecksumFilter(ExtendedAttributesHelper attributeHelper, PathResolver resolver) {

    this.attributeHelper = attributeHelper;
    this.resolver = resolver;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

    logger.debug("Initializing checksum filter.");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    addChecksumHeader((HttpServletRequest) request, (HttpServletResponse) response);

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {

    logger.debug("Destroying checksum filter.");
  }

  private void addChecksumHeader(HttpServletRequest request, HttpServletResponse response) {

    String method = request.getMethod().toUpperCase();

    if (!(method.equals("HEAD") || method.equals("GET"))) {

      // Skip if request is not a HEAD or a GET
      return;
    }

    logger.debug("Retrieving checksum value ...");
    String requestPath = format("%s%s", request.getServletPath(), request.getPathInfo());

    String pathResolved = resolver.resolvePath(requestPath);

    if (pathResolved == null) {
      logger.debug("Unable to resolve path {} to a local file", requestPath);
      return;
    }

    File f = new File(pathResolved);

    if (!f.exists()) {
      logger.debug("File {} doesn't exist", f);
      return;
    }

    if (f.isDirectory()) {
      logger.debug("{} is a directory: no checksum value to retrieve", f);
      return;
    }

    String checksumValue;

    try {

      checksumValue = attributeHelper.getChecksumAttribute(f);

    } catch (IOException e) {

      logger.error(
          "Error retrieving checksum value for path '{}': {}", pathResolved, e.getMessage());

      if (logger.isDebugEnabled()) {
        logger.error(e.getMessage(), e);
      }
      return;
    }

    if (!StringUtils.hasText(checksumValue)) {
      logger.warn("Null or empty checksum value for path: {}", pathResolved);
      return;
    }

    final String checksumHeaderContent = "adler32=" + checksumValue;
    response.setHeader("Digest", checksumHeaderContent);
  }
}
