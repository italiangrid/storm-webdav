// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SciTagFilter implements Filter {

  public static final Logger logger = LoggerFactory.getLogger(SciTagFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    if (req.getHeader(SciTag.SCITAG_HEADER) != null) {
      Optional<String> source = Optional.ofNullable(req.getHeader(TransferConstants.SOURCE_HEADER));
      boolean remoteAddressIsSource =
          req.getMethod().equals("PUT") || (req.getMethod().equals("COPY") && source.isPresent());
      // state prot src_ip src_port dst_ip dst_port exp act
      // If the active party receives an HTTP-TPC COPY request with a SciTag request header with
      // a valid value then the server SHOULD mark the resulting network traffic with the
      // experiment ID and activity ID encoded in the value.
      int scitagValue = Integer.parseInt(req.getHeader(SciTag.SCITAG_HEADER));
      // Valid value is a single positive integer > 64 and <65536 (16bit). Any other value is
      // considered invalid.
      if (scitagValue > 64 && scitagValue < 65536) {
        request.setAttribute(
            SciTag.SCITAG_ATTRIBUTE,
            new SciTag(scitagValue >> 6, scitagValue & ((1 << 6) - 1), remoteAddressIsSource));
      } else {
        // If the active party receives an HTTP-TPC COPY request with a SciTag request header
        // with an invalid value then the server SHOULD mark the resulting network traffic with
        // the 0 as the experiment ID and the activity ID.
        request.setAttribute(SciTag.SCITAG_ATTRIBUTE, new SciTag(0, 0, remoteAddressIsSource));
      }
    }
    chain.doFilter(request, response);
  }
}
