package org.italiangrid.storm.webdav.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChecksumFilter implements Filter {

  private final ExtendedAttributesHelper attributeHelper;
  private final PathResolver resolver;
  
  public static final Logger logger = LoggerFactory.getLogger(ChecksumFilter.class);
  
  public ChecksumFilter(ExtendedAttributesHelper attributeHelper,
    PathResolver resolver) {

    this.attributeHelper = attributeHelper;
    this.resolver = resolver;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

    logger.debug("Initializing checksum filter.");

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException {

    addChecksumHeader((HttpServletRequest) request, (HttpServletResponse) response);
    
    chain.doFilter(request, response);
    
  }

  @Override
  public void destroy() {

    logger.debug("Destroying checksum filter.");

  }
  
  private void addChecksumHeader(HttpServletRequest request,
    HttpServletResponse response) {

    String method = request.getMethod().toUpperCase();
    
    if (!(method.equals("HEAD") || method.equals("GET"))) {
      
      // Skip if request is not a HEAD or a GET
      return;
    }
    
    logger.debug("Retrieving checksum value ...");

    String pathResolved = resolver.resolvePath(request.getPathInfo());
      
    if (pathResolved == null) {
        
      logger.debug("Unable to resolve {} to a file", request.getPathInfo());
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
    
    try {

      String checksumValue = attributeHelper.getChecksumAttribute(f);

      logger.debug("Checksum value for file {} is '{}'", f, checksumValue);

      if (!checksumValue.isEmpty()) {

        response.setHeader("Digest", "adler32=" + checksumValue);

      }

    } catch (IOException e) {

      logger.error("Unable to retrieve file checksum value: {}",
        e.getMessage(), e);

    }

  }

}
