// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.authz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.italiangrid.storm.webdav.authz.util.CustomHttpMethodMatcher;
import org.italiangrid.storm.webdav.authz.util.EmptyPathRequestMatcher;
import org.italiangrid.storm.webdav.authz.util.StructuredPathScopeMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthzUtilsTest {

  @Mock HttpServletRequest request;

  @Test
  void testEmptyPathRequestMatcher() {
    EmptyPathRequestMatcher emptyMatcher = new EmptyPathRequestMatcher();
    assertEquals("EmptyPathRequestMatcher", emptyMatcher.toString());

    when(request.getServletPath()).thenReturn("");
    when(request.getPathInfo()).thenReturn("");
    assertTrue(emptyMatcher.matches(request));

    when(request.getServletPath()).thenReturn("/example");
    when(request.getPathInfo()).thenReturn("/file");
    assertFalse(emptyMatcher.matches(request));

    when(request.getPathInfo()).thenReturn(null);
    assertFalse(emptyMatcher.matches(request));
  }

  @Test
  void testStructuredPathScopeMatcherMatchesScope() {
    StructuredPathScopeMatcher scopeMatcher =
        StructuredPathScopeMatcher.fromString("storage.read:/path/file");
    assertThrows(NullPointerException.class, () -> scopeMatcher.matchesScope(null));
    assertThrows(
        IllegalArgumentException.class,
        () -> scopeMatcher.matchesScope("storage.read:/path/../otherFile"));
    assertTrue(scopeMatcher.matchesScope("storage.read:/path/file"));
    assertFalse(scopeMatcher.matchesScope("storage.write:/path/file"));
    assertFalse(scopeMatcher.matchesScope("storage.read:/otherPath/file"));
  }

  @Test
  void testStructuredPathScopeMatcherConstruction() {
    assertThrows(
        IllegalArgumentException.class,
        () -> StructuredPathScopeMatcher.structuredPathMatcher(null, "/path"));
    assertThrows(
        IllegalArgumentException.class,
        () -> StructuredPathScopeMatcher.structuredPathMatcher("storage.read:", "/path"));
    assertThrows(
        IllegalArgumentException.class,
        () -> StructuredPathScopeMatcher.structuredPathMatcher("storage.read", null));
    assertThrows(
        IllegalArgumentException.class,
        () -> StructuredPathScopeMatcher.structuredPathMatcher("storage.read", "path"));
    StructuredPathScopeMatcher scopeMatcher =
        StructuredPathScopeMatcher.structuredPathMatcher("storage.read", "/path/file");
    assertTrue(scopeMatcher.matchesScope("storage.read:/path/file"));
    assertEquals("storage.read:/path/file", scopeMatcher.toString());
    assertEquals("/path/file", scopeMatcher.getPath());
  }

  @Test
  void testStructuredPathScopeMatcherEquals() {
    StructuredPathScopeMatcher scopeMatcher =
        StructuredPathScopeMatcher.fromString("storage.read:/path/file");
    assertEquals(scopeMatcher, scopeMatcher);
    assertNotEquals(null, scopeMatcher);
    assertNotEquals(scopeMatcher, new String("storage.read:/path/file"));

    StructuredPathScopeMatcher scopeMatcher2 =
        StructuredPathScopeMatcher.fromString("storage.read:/path/otherFile");
    assertNotEquals(scopeMatcher, scopeMatcher2);

    scopeMatcher2 = StructuredPathScopeMatcher.fromString("storage.write:/path/file");
    assertNotEquals(scopeMatcher, scopeMatcher2);

    scopeMatcher2 = StructuredPathScopeMatcher.fromString("storage.read:/path/file");
    assertEquals(scopeMatcher, scopeMatcher2);
  }

  @Test
  void testStructuredPathScopeMatcherHash() {
    StructuredPathScopeMatcher scopeMatcher =
        StructuredPathScopeMatcher.fromString("storage.read:/path/file");
    assertEquals(
        (31 + Path.of("/path/file").hashCode()) * 31 + "storage.read".hashCode(),
        scopeMatcher.hashCode());
  }

  @Test
  void testCustomHttpMethodMatcher() {
    when(request.getMethod()).thenReturn(null);
    Set<String> methods = new HashSet<>(Arrays.asList("GET", "PUT"));
    CustomHttpMethodMatcher matcher = new CustomHttpMethodMatcher(methods);

    Set<String> supported = matcher.getSupportedMethods();
    assertTrue(supported.contains("GET"));
    assertTrue(supported.contains("PUT"));
    assertEquals(2, supported.size());
    assertFalse(matcher.matches(request));
  }
}
