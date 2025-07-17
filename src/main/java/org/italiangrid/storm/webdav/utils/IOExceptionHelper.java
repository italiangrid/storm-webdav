// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.utils;

import java.io.IOException;
import org.italiangrid.storm.webdav.error.DiskQuotaExceeded;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;

public final class IOExceptionHelper {

  public static final String DISK_QUOTA_EXCEEDED = "Disk quota exceeded";

  private IOExceptionHelper() {}

  public static StoRMWebDAVError getStoRMWebDAVError(IOException e) {
    if (DISK_QUOTA_EXCEEDED.equals(e.getMessage())) {
      return new DiskQuotaExceeded(e.getMessage(), e);
    }
    return new StoRMWebDAVError(e.getMessage(), e);
  }
}
