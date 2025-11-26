// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.utils;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.util.StringUtils;

public final class Adler32DigestHeaderHelper {

  public static final String DIGEST_HEADER = "Digest";
  public static final String DIGEST_HEADER_REGEX = "^\\s*adler32\\s*=\\s*([0-9a-zA-Z]{8})\\s*";
  public static final Pattern DIGEST_HEADER_PATTERN = Pattern.compile(DIGEST_HEADER_REGEX);
  public static final String REPR_DIGEST_HEADER_REGEX =
      ".*adler(?:32)?\\s*=\\s*:([0-9a-zA-Z+/]+=*):.*";
  public static final Pattern REPR_DIGEST_HEADER_PATTERN =
      Pattern.compile(REPR_DIGEST_HEADER_REGEX);

  private Adler32DigestHeaderHelper() {}

  public static Optional<String> extractAdler32DigestFromResponse(HttpResponse response) {

    Objects.requireNonNull(response);

    Optional<Header> digestHeader = Optional.ofNullable(response.getFirstHeader(DIGEST_HEADER));

    if (digestHeader.isPresent()) {

      String digestHeaderValue = digestHeader.get().getValue();

      if (StringUtils.hasText(digestHeaderValue)) {
        Matcher m = DIGEST_HEADER_PATTERN.matcher(digestHeaderValue);

        if (m.matches()) {
          return Optional.of(m.group(1));
        }
      }
    }
    return Optional.empty();
  }

  public static Optional<String> extractAdler32DigestFromHeaderValue(String headerValue) {
    return Optional.ofNullable(headerValue)
        .map(REPR_DIGEST_HEADER_PATTERN::matcher)
        .filter(Matcher::matches)
        .map(m -> m.group(1))
        .map(Base64.getDecoder()::decode)
        .map(String::new);
  }
}
