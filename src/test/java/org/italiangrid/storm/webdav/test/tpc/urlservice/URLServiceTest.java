/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.test.tpc.urlservice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.italiangrid.storm.webdav.tpc.StaticHostListLocalURLService;
import org.italiangrid.storm.webdav.tpc.URLResolutionError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class URLServiceTest {

  public static final String[] SERVICE_ALIASES =
      {"storm.example", "alias.storm.example", "localhost"};


  @Test(expected = IllegalArgumentException.class)
  public void testEmptyList() {
    new StaticHostListLocalURLService(Collections.emptyList());
  }

  @Test(expected = NullPointerException.class)
  public void testNullList() {
    new StaticHostListLocalURLService(null);
  }

  @Test
  public void testResolution() {
    StaticHostListLocalURLService service =
        new StaticHostListLocalURLService(Arrays.asList(SERVICE_ALIASES));

    assertThat(service.isLocalURL("https://remote.org:833"), is(false));
    assertThat(service.isLocalURL("http://152.158.1.1"), is(false));
    assertThat(service.isLocalURL("file://storm.example"), is(true));
    assertThat(service.isLocalURL("https://storm.example"), is(true));
    assertThat(service.isLocalURL("file://alias.storm.example"), is(true));
    assertThat(service.isLocalURL("https://localhost"), is(true));
    assertThat(service.isLocalURL("/storage/f"), is(true));

  }

  @Test(expected = URLResolutionError.class)
  public void testInvalidUrlResolution() {
    StaticHostListLocalURLService service =
        new StaticHostListLocalURLService(Arrays.asList(SERVICE_ALIASES));

    assertThat(service.isLocalURL("http://example.invalid/sososo[/]"), is(false));

  }
}
