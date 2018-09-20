package org.italiangrid.storm.webdav.server.util;

import org.italiangrid.storm.webdav.error.StoRMIntializationError;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class StormFailureAnalyzer extends AbstractFailureAnalyzer<StoRMIntializationError> {

  @Override
  protected FailureAnalysis analyze(Throwable rootFailure, StoRMIntializationError cause) {
    return new FailureAnalysis(getFailureDescription(cause), "Check the WebDAV service configuration!", cause);
  }

  protected String getFailureDescription(StoRMIntializationError cause) {
    return String.format("Storm WebDAV could not start due to an error: %s", cause.getMessage());
  }

}
