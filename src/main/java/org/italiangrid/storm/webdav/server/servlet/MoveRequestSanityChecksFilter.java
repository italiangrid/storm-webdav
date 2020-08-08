package org.italiangrid.storm.webdav.server.servlet;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.error.BadRequest;
import org.italiangrid.storm.webdav.error.ResourceNotFound;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class MoveRequestSanityChecksFilter implements Filter, TpcUtils {

  private final PathResolver resolver;

  @Autowired
  public MoveRequestSanityChecksFilter(PathResolver resolver) {
    this.resolver = resolver;
  }

  private void moveSanityChecks(HttpServletRequest req) throws MalformedURLException {
    if (WebDAVMethod.MOVE.name().equals(req.getMethod()) && requestHasDestinationHeader(req)
        && !requestPathAndDestinationHeaderAreInSameStorageArea(req, resolver)) {
      throw new BadRequest("Move across storage areas is not supported");
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    try {
      moveSanityChecks(req);
    } catch (MalformedURLException | BadRequest | ResourceNotFound e) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.setContentType("text/plain");
      res.getWriter().print(e.getMessage());
      res.flushBuffer();
      return;
    }

    chain.doFilter(request, response);

  }

}
