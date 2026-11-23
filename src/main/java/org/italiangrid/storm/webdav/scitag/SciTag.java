// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.scitag;

public record SciTag(int experimentId, int activityId, boolean remoteAddressIsSource) {
  public static final String SCITAG_HEADER = "SciTag";
  public static final String SCITAG_ATTRIBUTE = "scitag";
}
