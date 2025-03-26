// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.fs;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface Libc extends Library {
  public static final Libc INSTANCE = Native.load("c", Libc.class);

  int stat(String path, Stat stat);
}
