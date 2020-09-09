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
package org.italiangrid.storm.webdav.tpc.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

public class Adler32DigestHeaderHelper {

  public static final String DIGEST_HEADER = "Digest";
  public static final String DIGEST_HEADER_REGEX = "^\\s*adler32\\s*=\\s*([0-9a-zA-Z]{8})\\s*";
  public static final Pattern DIGEST_HEADER_PATTERN = Pattern.compile(DIGEST_HEADER_REGEX);

  public static Optional<String> extractAdler32DigestFromResponse(HttpResponse response) {

    checkNotNull(response);

    Optional<Header> digestHeader = Optional.ofNullable(response.getFirstHeader(DIGEST_HEADER));

    if (digestHeader.isPresent()) {

      String digestHeaderValue = digestHeader.get().getValue();
      
      if (!isNullOrEmpty(digestHeaderValue)) {
        Matcher m = DIGEST_HEADER_PATTERN.matcher(digestHeaderValue);

        if (m.matches()) {
          return Optional.of(m.group(1));
        }
      }
    }
    return Optional.empty();
  }

}
