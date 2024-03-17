/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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
package org.italiangrid.storm.webdav.test.oauth.jwt;

import static org.junit.Assert.assertEquals;

import org.italiangrid.storm.webdav.oauth.utils.NoExpirationStringCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NoExpirationStringCacheTests {

  final String CACHED_VALUE = "this-is-my-cached-value";
  final String FAKE_ISSUER = "http://localhost";

  @Test
  public void noExpirationCacheWorks() {

    NoExpirationStringCache cache = new NoExpirationStringCache(CACHED_VALUE);

    assertEquals("NoExpirationCache", cache.getName());
    assertEquals(cache, cache.getNativeCache());
    assertEquals(CACHED_VALUE, cache.get(FAKE_ISSUER).get());
    cache.clear();
    cache.put(FAKE_ISSUER, CACHED_VALUE);
    assertEquals(CACHED_VALUE, cache.get(FAKE_ISSUER).get());
    cache.evict(FAKE_ISSUER);
    assertEquals(CACHED_VALUE, cache.get(FAKE_ISSUER).get());
  }
}
