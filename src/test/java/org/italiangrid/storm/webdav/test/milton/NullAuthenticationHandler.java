// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.milton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.milton.http.Request;
import io.milton.resource.Resource;
import org.italiangrid.storm.webdav.milton.NullAuthenticationHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class NullAuthenticationHandlerTests {
  private final NullAuthenticationHandler handler = new NullAuthenticationHandler();

  @Mock Resource resource;
  @Mock Request request;

  @Test
  void supportTest() {
    assertTrue(handler.supports(resource, request));
  }

  @Test
  void authenticateTest() {
    assertEquals(NullAuthenticationHandler.STORM_USER, handler.authenticate(resource, request));
  }

  @Test
  void isCompatibleTest() {
    assertTrue(handler.isCompatible(resource, request));
  }

  @Test
  void credentialsPresent() {
    assertTrue(handler.credentialsPresent(request));
  }
}
