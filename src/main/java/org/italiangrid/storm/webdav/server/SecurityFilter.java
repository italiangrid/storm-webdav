package org.italiangrid.storm.webdav.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityFilter implements Filter {

  public static final Logger log = LoggerFactory
    .getLogger(SecurityFilter.class);

  @Override
  public void destroy() {

    log.debug("Destroying security filter.");

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException {

    chain.doFilter(request, response);

  }

  @Override
  public void init(FilterConfig config) throws ServletException {

    log.debug("Initializing security filter.");

  }

}
