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
    requestId.set(null);
  }
}
