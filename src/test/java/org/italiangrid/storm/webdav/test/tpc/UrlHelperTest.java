package org.italiangrid.storm.webdav.test.tpc;


import static java.lang.String.format;
import static org.italiangrid.storm.webdav.tpc.utils.UrlHelper.isRemoteUrl;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UrlHelperTest {

  public static final String[] LOCAL_URLS = {"/", "some/relative/path", "/some/absolute/path"};

  public static final String[] REMOTE_URLS = {"http://storm.example/test/file.txt",
      "https://storm.example/test/file.txt", "httpg://storm.example/test/file.txt",
      "dav://storm.example/test/file.txt", "davs://storm.example/test/file.txt"};


  @Test
  public void urlHelperTest() {
    
    for (String u : LOCAL_URLS) {
      assertFalse(format("%s considered a remote URL!", u), isRemoteUrl(u));
    }

    for (String u : REMOTE_URLS) {
      assertTrue(format("%s considered a local URL!", u), isRemoteUrl(u));
    }
  }

}
