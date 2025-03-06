// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.utils;

import java.util.function.Supplier;

public interface TestUtils {

  default Supplier<AssertionError> assertionError(String msg) {
    return () -> new AssertionError(msg);
  }
}
