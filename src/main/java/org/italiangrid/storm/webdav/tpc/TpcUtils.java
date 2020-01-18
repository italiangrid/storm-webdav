/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

public interface TpcUtils extends TransferConstants {



  default boolean requestHasSourceHeader(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(SOURCE_HEADER)).isPresent();
  }

  default boolean requestHasRemoteDestinationHeader(HttpServletRequest request,
      LocalURLService localURLService) {
    Optional<String> destination = Optional.ofNullable(request.getHeader(DESTINATION_HEADER));

    return (destination.isPresent() && !localURLService.isLocalURL(destination.get()));
  }


  default boolean isPullTpc(HttpServletRequest request, LocalURLService localUrlService) {
    return "COPY".equals(request.getMethod()) && requestHasSourceHeader(request);
  }

  default boolean isTpc(HttpServletRequest request, LocalURLService localUrlService) {
    return "COPY".equals(request.getMethod()) && (requestHasSourceHeader(request)
        || requestHasRemoteDestinationHeader(request, localUrlService));
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
