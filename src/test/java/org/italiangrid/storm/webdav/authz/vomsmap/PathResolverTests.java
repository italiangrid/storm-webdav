package org.italiangrid.storm.webdav.authz.vomsmap;

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
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PathResolverTests {

  private final String ROOTDIR = "/storage";

  /*
   * Map SA-name -> VO name
   */
  private static Map<String, String> input;

  static {

    input = new HashMap<String, String>();
    input.put("test.vo.bis", "testers.eu-emi.eu");
    input.put("test.vo", "test.vo");
    input.put("test1", "testers.eu-emi.eu");
    input.put("test12", "test.vo");
    input.put("12test.", "testers.eu-emi.eu");
    input.put("12test", "test.vo");

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
  public void checkPathResolver() {

    for (String name : input.keySet()) {

      String pathToTest = "/".concat(name).concat("/testdir");
      String expectedRootPath = ROOTDIR.concat("/").concat(input.get(name))
        .concat("/testdir");

      checkResolvedRootPath(pathToTest, expectedRootPath);
      checkResolvedStorageArea(pathToTest, expectedRootPath);

    }

  }

  private void checkResolvedRootPath(String pathToTest, String expectedRootPath) {

    String rootPath = pathResolver.resolvePath(pathToTest);
    Assert.assertEquals(expectedRootPath, rootPath);
  }

  private void checkResolvedStorageArea(String pathToTest,
    String expectedRootPath) {

    StorageAreaInfo sa = pathResolver.resolveStorageArea(pathToTest);
    Assert.assertEquals(expectedRootPath, sa.rootPath() + "/testdir");
  }

}
