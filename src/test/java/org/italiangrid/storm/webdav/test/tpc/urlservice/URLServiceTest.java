// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc.urlservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.italiangrid.storm.webdav.tpc.StaticHostListLocalURLService;
import org.italiangrid.storm.webdav.tpc.URLResolutionError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class URLServiceTest {

  public static final String[] SERVICE_ALIASES = {
    "storm.example", "alias.storm.example", "localhost"
  };

  @Test
  void testEmptyList() {

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new StaticHostListLocalURLService(Collections.emptyList());
        });
  }

  @Test
  void testNullList() {

    assertThrows(
        NullPointerException.class,
        () -> {
          new StaticHostListLocalURLService(null);
        });
  }

  @Test
  void testResolution() {
    StaticHostListLocalURLService service =
        new StaticHostListLocalURLService(Arrays.asList(SERVICE_ALIASES));

    assertFalse(service.isLocalURL("https://remote.org:833"));
    assertFalse(service.isLocalURL("http://152.158.1.1"));
    assertTrue(service.isLocalURL("file://storm.example"));
    assertTrue(service.isLocalURL("https://storm.example"));
    assertTrue(service.isLocalURL("file://alias.storm.example"));
    assertTrue(service.isLocalURL("https://localhost"));
    assertTrue(service.isLocalURL("/storage/f"));
  }

  @Test
  void testInvalidUrlResolution() {

    StaticHostListLocalURLService service =
        new StaticHostListLocalURLService(Arrays.asList(SERVICE_ALIASES));

    assertThrows(
        URLResolutionError.class,
        () -> {
          service.isLocalURL("http://example.invalid/sososo[/]");
        });
  }
}
