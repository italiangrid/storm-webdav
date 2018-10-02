/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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
package org.italiangrid.storm.webdav.test.authz.vomap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.DefaultPathResolver;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PathResolverTests {

  private final String ROOTDIR = "/storage";

  /*
   * Map SA-name -> VO name
   */
  private static Map<String, String> input;

  @BeforeClass
  public static void init() {

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

  @Before
  public void setup() {

    saInfoList = new ArrayList<StorageAreaInfo>();
    for (String ap : input.keySet()) {
      saInfoList.add(getMockSAInfo(ap, input.get(ap)));
    }

    saConfig = mock(StorageAreaConfiguration.class);
    when(saConfig.getStorageAreaInfo()).thenReturn(saInfoList);

    pathResolver = new DefaultPathResolver(saConfig);
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
  public void checkResolvedRootPath() {

    for (String name : input.keySet()) {

      String pathToTest = "/".concat(name).concat("/testdir");
      String expectedRootPath = ROOTDIR.concat("/").concat(input.get(name))
        .concat("/testdir");
      
      String rootPath = pathResolver.resolvePath(pathToTest);
      Assert.assertEquals(expectedRootPath, rootPath);
    }

  }
  
  @Test
  public void checkResolvedStorageArea() {

    for (String name : input.keySet()) {

      String pathToTest = "/".concat(name).concat("/testdir");
      String expectedRootPath = ROOTDIR.concat("/").concat(input.get(name))
        .concat("/testdir");
      
      StorageAreaInfo sa = pathResolver.resolveStorageArea(pathToTest);
      Assert.assertEquals(expectedRootPath, sa.rootPath() + "/testdir");
    }
  }

}
