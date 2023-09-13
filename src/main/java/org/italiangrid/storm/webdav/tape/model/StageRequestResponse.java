package org.italiangrid.storm.webdav.tape.model;

public class StageRequestResponse {

  private String requestId;

  public StageRequestResponse(String id) {
    requestId = id;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

}