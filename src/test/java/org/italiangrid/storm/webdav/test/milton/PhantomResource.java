// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.milton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.Date;
import org.italiangrid.storm.webdav.milton.PhantomResource;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class PhantomResourceTests {
  private static final String PATH = "/path/to/test";
  private final PhantomResource resource = new PhantomResource(PATH);

  @Mock Request request;
  @Mock Method method;
  @Mock Auth auth;

  @Test
  void constructorTest() {
    assertEquals(PATH, resource.getName());
  }

  @Test
  void getUniqueIdTest() {
    assertEquals(null, resource.getUniqueId());
  }

  @Test
  void authenticateTest() {
    assertEquals(null, resource.authenticate("user", "password"));
  }

  @Test
  void authoriseTest() {
    assertFalse(resource.authorise(request, method, auth));
  }

  @Test
  void getReamTest() {
    assertEquals(null, resource.getRealm());
  }

  @Test
  void getModifiedDate() {
    assertEquals(new Date(), resource.getModifiedDate());
  }

  @Test
  void checkRedirectTest() throws NotAuthorizedException, BadRequestException {
    assertEquals(null, resource.checkRedirect(request));
  }
}
