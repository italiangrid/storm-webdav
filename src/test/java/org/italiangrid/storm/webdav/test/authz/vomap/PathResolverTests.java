// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.authz.vomap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.fs.Locality;
import org.italiangrid.storm.webdav.fs.attrs.DefaultExtendedFileAttributesHelper;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.DefaultPathResolver;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PathResolverTests {

  private static final String ROOTDIR = "/storage";

  /*
   * Map SA-name -> VO name
   */
  private static Map<String, String> input;

  @BeforeAll
  static void init() {

    input = new HashMap<String, String>();
    input.put("test.vo.bis", "testers.eu-emi.eu");
    input.put("test.vo", "test.vo");
    input.put("test1", "testers.eu-emi.eu");
    input.put("test12", "test.vo");
    input.put("12test.", "testers.eu-emi.eu");
    input.put("12test", "test.vo");
    input.put("1", "testers.eu-emi.eu");
    input.put("12", "test.vo");
    input.put(".", "testers.eu-emi.eu");
    input.put(".1", "test.vo");
  }

  PathResolver pathResolver;
  StorageAreaConfiguration saConfig;
  List<StorageAreaInfo> saInfoList;

  @BeforeEach
  void setup() {

    saInfoList = new ArrayList<StorageAreaInfo>();
    for (String ap : input.keySet()) {
      saInfoList.add(getMockSAInfo(ap, input.get(ap)));
    }

    saConfig = mock(StorageAreaConfiguration.class);
    when(saConfig.getStorageAreaInfo()).thenReturn(saInfoList);

    ExtendedAttributesHelper attributesHelper = new DefaultExtendedFileAttributesHelper();

    pathResolver = new DefaultPathResolver(saConfig, attributesHelper);
  }

  private StorageAreaInfo getMockSAInfo(String name, String voname) {

    StorageAreaInfo saInfo = mock(StorageAreaInfo.class);

    String ap = "/".concat(name);
    String rootPath = ROOTDIR.concat("/").concat(voname);

    when(saInfo.name()).thenReturn(name);
    when(saInfo.accessPoints()).thenReturn(Arrays.asList(ap));
    when(saInfo.rootPath()).thenReturn(rootPath);
    when(saInfo.vos()).thenReturn(new HashSet<String>(Arrays.asList(voname)));

    return saInfo;
  }

  @Test
  void checkResolvedRootPath() {

    for (String name : input.keySet()) {

      String pathToTest = "/".concat(name).concat("/testdir");
      String expectedRootPath = ROOTDIR.concat("/").concat(input.get(name)).concat("/testdir");

      String rootPath = pathResolver.resolvePath(pathToTest);
      Assert.assertEquals(expectedRootPath, rootPath);
    }
  }

  @Test
  void checkResolvedStorageArea() {

    for (String name : input.keySet()) {

      String pathToTest = "/".concat(name).concat("/testdir");
      String expectedRootPath = ROOTDIR.concat("/").concat(input.get(name)).concat("/testdir");

      StorageAreaInfo sa = pathResolver.resolveStorageArea(pathToTest);
      Assert.assertEquals(expectedRootPath, sa.rootPath() + "/testdir");
    }
  }

  @Test
  void checkFileLocality() {
    Locality locality = pathResolver.getLocality("/test1/notATapeStorageArea/normalFile.txt");
    Assert.assertEquals(Locality.DISK, locality);
  }
}
