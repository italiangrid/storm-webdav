// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.utils;

import java.io.IOException;
import java.util.Set;
import org.italiangrid.storm.webdav.error.InsufficientStorage;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;

public final class IOExceptionHelper {

  public static final Set<String> INSUFFICIENT_STORAGE_MESSAGES =
      Set.of("Disk quota exceeded", "No space left on device");

  private IOExceptionHelper() {}

  public static StoRMWebDAVError getStoRMWebDAVError(IOException e) {
    if (e.getMessage() != null && INSUFFICIENT_STORAGE_MESSAGES.contains(e.getMessage())) {
      return new InsufficientStorage(e.getMessage(), e);
    }
    return new StoRMWebDAVError(e.getMessage(), e);
  }
}
