// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.tracing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ServerTracingTests {

  @Test
  void testRequestIdHolder() {
    final String uuid = "0c55ba7f-40da-4593-8a42-9f07c83d98f1";

    RequestIdHolder.setRequestId(uuid);
    assertEquals(uuid, RequestIdHolder.getRequestId());

    RequestIdHolder.cleanup();
    assertEquals(null, RequestIdHolder.getRequestId());
  }
}
