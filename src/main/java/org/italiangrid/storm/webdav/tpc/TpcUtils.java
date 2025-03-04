// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc;

import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import jakarta.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.error.ResourceNotFound;
import org.italiangrid.storm.webdav.server.PathResolver;

public interface TpcUtils {

  default Supplier<ResourceNotFound> resourceNotFoundError(String path) {
    return () -> new ResourceNotFound(format("No storage area found matching path: %s", path));
  }

  default String getSerlvetRequestPath(HttpServletRequest request) {
    String url = request.getServletPath();

    if (request.getPathInfo() != null) {
      url += request.getPathInfo();
    }

    return url.replaceAll("\\/+$", ""); // Drop trailing slashes
  }

  default String destinationHeader(HttpServletRequest request) {
    return request.getHeader(TransferConstants.DESTINATION_HEADER);
  }

  default boolean requestHasSourceHeader(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(TransferConstants.SOURCE_HEADER)).isPresent();
  }

  default boolean requestHasDestinationHeader(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(TransferConstants.DESTINATION_HEADER)).isPresent();
  }

  default boolean requestHasLocalDestinationHeader(HttpServletRequest request,
      LocalURLService localURLService) {
    Optional<String> destination =
        Optional.ofNullable(request.getHeader(TransferConstants.DESTINATION_HEADER));

    return (destination.isPresent() && localURLService.isLocalURL(destination.get()));
  }

  default boolean requestHasRemoteDestinationHeader(HttpServletRequest request,
      LocalURLService localURLService) {
    Optional<String> destination =
        Optional.ofNullable(request.getHeader(TransferConstants.DESTINATION_HEADER));

    return (destination.isPresent() && !localURLService.isLocalURL(destination.get()));
  }

  default boolean requestPathAndDestinationHeaderAreInSameStorageArea(HttpServletRequest request,
      PathResolver resolver) throws MalformedURLException {
    final String source = getSerlvetRequestPath(request);
    final String destination = getSanitizedPathFromUrl(destinationHeader(request));

    StorageAreaInfo sourceSa = Optional.ofNullable(resolver.resolveStorageArea(source))
      .orElseThrow(resourceNotFoundError(source));

    StorageAreaInfo destSa = Optional.ofNullable(resolver.resolveStorageArea(destination))
      .orElseThrow(resourceNotFoundError(destination));

    return sourceSa.equals(destSa);
  }

  default boolean pathIsStorageAreaRoot(HttpServletRequest request, PathResolver resolver) {
    final String path = getSerlvetRequestPath(request);
    StorageAreaInfo sa = Optional.ofNullable(resolver.resolveStorageArea(path))
      .orElseThrow(resourceNotFoundError(path));

    return sa.accessPoints().contains(path);
  }

  default boolean isPullTpc(HttpServletRequest request, LocalURLService localUrlService) {
    return "COPY".equals(request.getMethod()) && requestHasSourceHeader(request);
  }

  default boolean isCopy(HttpServletRequest request) {
    return "COPY".equals(request.getMethod())
        && (requestHasSourceHeader(request) || requestHasDestinationHeader(request));
  }

  default boolean isPushTpc(HttpServletRequest request, LocalURLService localUrlService) {
    return "COPY".equals(request.getMethod())
        && (requestHasRemoteDestinationHeader(request, localUrlService)
            || requestHasTranferHeader(request));
  }

  default boolean requestHasTranferHeader(HttpServletRequest request) {
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if (headerName.toLowerCase().startsWith(TransferConstants.TRANSFER_HEADER_LC)) {
        return true;
      }
    }
    return false;
  }

  default boolean isTpc(HttpServletRequest request, LocalURLService localUrlService) {
    return "COPY".equals(request.getMethod()) && (requestHasSourceHeader(request)
        || requestHasRemoteDestinationHeader(request, localUrlService)
        || requestHasTranferHeader(request));
  }

  default boolean isCopyOrMoveRequest(HttpServletRequest request) {
    return "COPY".equals(request.getMethod()) || "MOVE".equals(request.getMethod());
  }

  default String dropSlashWebdavFromPath(String path) {
    Matcher m = TransferConstants.WEBDAV_PATH_PATTERN.matcher(path);

    if (m.matches()) {
      return String.format("/%s", m.group(1));
    }

    return path;
  }

  default String getSanitizedPathFromUrl(String destinationUrl) throws MalformedURLException {
    URL url = new URL(destinationUrl);
    return dropSlashWebdavFromPath(url.getPath());
  }



}
