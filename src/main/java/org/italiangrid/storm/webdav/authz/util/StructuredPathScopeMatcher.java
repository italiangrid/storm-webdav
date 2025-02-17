// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.util;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.Generated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class StructuredPathScopeMatcher implements ScopeMatcher {

  public static final Logger LOG = LoggerFactory.getLogger(StructuredPathScopeMatcher.class);

  private static final Pattern POINT_DIR_MATCHER = Pattern.compile("\\.\\./?");

  private static final Character SEP = ':';
  private static final String SEP_STR = SEP.toString();

  private final String prefix;
  private final Path path;

  private final Pattern prefixMatchPattern;
  private final Pattern pathMatchPattern;

  private StructuredPathScopeMatcher(String prefix, String path) {

    this.prefix = prefix;
    this.path = Path.of(path);

    final String prefixMatchRegexp = String.format("^%s%c", prefix, SEP);
    prefixMatchPattern = Pattern.compile(prefixMatchRegexp);
    pathMatchPattern = Pattern.compile(path + ".*");
  }

  @Override
  public boolean matchesScope(String scope) {
    Objects.requireNonNull(scope, "scope must be non-null");

    if (POINT_DIR_MATCHER.matcher(scope).find()) {
      throw new IllegalArgumentException("Scope contains relative path references");
    }

    Matcher prefixMatcher = prefixMatchPattern.matcher(scope);

    boolean prefixMatches = prefixMatcher.find();

    if (prefixMatches) {
      final String scopePath = scope.substring(prefix.length() + 1);
      return pathMatchPattern.matcher(scopePath).matches();
    } else {
      return false;
    }
  }

  public boolean matchesPath(String path) {
    return pathMatchPattern.matcher(path).matches();
  }

  public boolean matchesPathIncludingParents(String path) {
    Path targetPath = Path.of(path);
    return this.path.startsWith(targetPath) || matchesPath(path);
  }

  public static StructuredPathScopeMatcher fromString(String scope) {
    final int sepIndex = scope.indexOf(SEP);
    final String prefix = scope.substring(0, sepIndex);
    final String path = scope.substring(sepIndex + 1, scope.length());
    return new StructuredPathScopeMatcher(prefix, path);
  }

  public static StructuredPathScopeMatcher structuredPathMatcher(String prefix, String path) {
    Assert.hasText(prefix, "empty or null prefix");
    Assert.doesNotContain(prefix, SEP_STR, "prefix must not contain context separator");
    Assert.hasText(path, "empty or null path");
    Assert.isTrue(path.startsWith("/"), "path must start with a /");

    return new StructuredPathScopeMatcher(prefix, path);
  }

  @Override
  public String toString() {
    return String.format("%s:%s", prefix, path);
  }

  @Override
  @Generated("eclipse")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
    return result;
  }

  @Override
  @Generated("eclipse")
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (getClass() != obj.getClass()) {
      return false;
    }
    StructuredPathScopeMatcher other = (StructuredPathScopeMatcher) obj;
    if (path == null) {
      if (other.path != null) {
        return false;
      }
    } else if (!path.equals(other.path)) {
      return false;
    }
    if (prefix == null) {
      if (other.prefix != null) {
        return false;
      }
    } else if (!prefix.equals(other.prefix)) {
      return false;
    }
    return true;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getPath() {
    return path.toString();
  }

}
