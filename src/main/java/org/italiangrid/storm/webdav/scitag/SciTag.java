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
package org.italiangrid.storm.webdav.scitag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SciTag {

  public static final Logger LOG = LoggerFactory.getLogger(SciTag.class);
  public static final String SCITAG_HEADER = "SciTag";
  public static final String SCITAG_ATTRIBUTE = "scitag";

  private final int experimentId;
  private final int activityId;
  private final boolean remoteAddressIsSource;

  public SciTag(int experimentId, int activityId, boolean remoteAddressIsSource) {
    this.experimentId = experimentId;
    this.activityId = activityId;
    this.remoteAddressIsSource = remoteAddressIsSource;
  }

  public int experimentId() {
    return experimentId;
  }

  public int activityId() {
    return activityId;
  }

  public boolean remoteAddressIsSource() {
    return remoteAddressIsSource;
  }

}
