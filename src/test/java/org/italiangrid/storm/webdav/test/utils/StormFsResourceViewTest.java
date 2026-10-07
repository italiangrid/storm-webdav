// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import org.italiangrid.storm.webdav.fs.Locality;
import org.italiangrid.storm.webdav.server.servlet.resource.StormFsResourceView;
import org.junit.jupiter.api.Test;

class StormFsResourceViewTest {
  @Test
  void testFileName() {
    StormFsResourceView resview =
        new StormFsResourceView("file.txt", false, "/tmp/file.txt", 0, new Date(), Locality.DISK);
    assertEquals("file.txt", resview.name());
  }

  @Test
  void testDirectoryName() {
    StormFsResourceView resview =
        new StormFsResourceView("dir", true, "/tmp/dir", 0, new Date(), Locality.DISK);
    assertEquals("dir/", resview.name());
    resview =
        new StormFsResourceView("dir_slash/", true, "/tmp/dir_slash", 0, new Date(), Locality.DISK);
    assertEquals("dir_slash/", resview.name());
  }
}
