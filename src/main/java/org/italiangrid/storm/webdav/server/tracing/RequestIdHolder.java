// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.tracing;

import java.util.UUID;

public class RequestIdHolder {

  private static ThreadLocal<String> requestId = new ThreadLocal<>();

  private RequestIdHolder() {}

  public static String getRequestId() {
    return requestId.get();
  }

  public static void setRequestId(String id) {
    requestId.set(id);
  }

  public static void setRandomId() {
    requestId.set(UUID.randomUUID().toString());
  }
  
  public static void cleanup() {
    requestId.remove();
  }
}
