/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.italiangrid.storm.webdav.tpc;

import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.error.ResourceNotFound;
import org.italiangrid.storm.webdav.server.PathResolver;

public interface TpcUtils extends TransferConstants {

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
    return request.getHeader(DESTINATION_HEADER);
  }

  default boolean requestHasSourceHeader(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(SOURCE_HEADER)).isPresent();
  }

  default boolean requestHasDestinationHeader(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(DESTINATION_HEADER)).isPresent();
  }

  default boolean requestHasLocalDestinationHeader(HttpServletRequest request,
      LocalURLService localURLService) {
    Optional<String> destination = Optional.ofNullable(request.getHeader(DESTINATION_HEADER));

    return (destination.isPresent() && localURLService.isLocalURL(destination.get()));
  }

  default boolean requestHasRemoteDestinationHeader(HttpServletRequest request,
      LocalURLService localURLService) {
    Optional<String> destination = Optional.ofNullable(request.getHeader(DESTINATION_HEADER));

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

  default boolean requestHasTranferHeader(HttpServletRequest request) {
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if (headerName.toLowerCase().startsWith(TRANSFER_HEADER_LC)) {
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
    Matcher m = WEBDAV_PATH_PATTERN.matcher(path);

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
