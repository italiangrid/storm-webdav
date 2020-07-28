package org.italiangrid.storm.webdav.server.servlet;

import static java.lang.String.format;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.authz.util.MatcherUtils;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.error.BadRequest;
import org.italiangrid.storm.webdav.error.ResourceNotFound;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class MoveRequestSanityChecksFilter implements Filter, TpcUtils, MatcherUtils {

  private final PathResolver resolver;

  @Autowired
  public MoveRequestSanityChecksFilter(PathResolver resolver) {
    this.resolver = resolver;
  }

  Supplier<ResourceNotFound> resourceNotFoundError(String path) {
    return () -> new ResourceNotFound(format("No storage area found matching path: %s", path));
  }


  private void moveSanityChecks(HttpServletRequest request) throws MalformedURLException {
    if (WebDAVMethod.MOVE.name().equals(request.getMethod())
        && requestHasDestinationHeader(request)) {

      final String source = getRequestPath(request);
      final String destination = getSanitizedPathFromUrl(destinationHeader(request));

      StorageAreaInfo sourceSa = Optional.ofNullable(resolver.resolveStorageArea(source))
        .orElseThrow(resourceNotFoundError(source));

      StorageAreaInfo destSa = Optional.ofNullable(resolver.resolveStorageArea(destination))
        .orElseThrow(resourceNotFoundError(destination));

      if (!sourceSa.equals(destSa)) {
        throw new BadRequest("Move across storage areas is not supported");
      }
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
