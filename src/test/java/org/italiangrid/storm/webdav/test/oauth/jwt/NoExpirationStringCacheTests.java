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
