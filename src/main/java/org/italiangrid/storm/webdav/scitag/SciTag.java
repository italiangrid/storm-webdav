// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
