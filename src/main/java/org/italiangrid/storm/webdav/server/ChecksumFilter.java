package org.italiangrid.storm.webdav.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChecksumFilter implements Filter {

  public static final Logger log = LoggerFactory
    .getLogger(ChecksumFilter.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

    log.debug("Initializing checksum filter.");
    
  }

  private boolean needChecksum(String method) {
    
    return method.equals("HEAD") || method.equals("GET");
  }
  
  private boolean isSuccess(int status) {
    
    return status == 200;
  }
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException {

    chain.doFilter(request, response);

    log.debug("Executing checksum filter");
    
    HttpServletRequest req = (HttpServletRequest) request;
    
    String method = req.getMethod().toUpperCase();
    
    if (!needChecksum(method)) {
      
      log.debug("Checksum doesn't need to be returned. Method is %s", method);
      
      return;
    }
    
    HttpServletResponse res = (HttpServletResponse) response;
     
    if (!isSuccess(res.getStatus())) {
      
      log.debug("Checksum doesn't need to be returned. Response is not a success!");
      
      return;
    }  
      
    log.debug("Retrieving checksum as digest");
    
    res.setHeader("Digest", "qui ci sta il checksum");
    
  }

  @Override
  public void destroy() {

    log.debug("Destroying checksum filter.");
    
  }
  
}
