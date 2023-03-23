/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
