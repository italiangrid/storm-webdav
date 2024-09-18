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
package org.italiangrid.storm.webdav.oauth.utils;

import java.util.concurrent.Callable;

import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.lang.Nullable;

public class NoExpirationStringCache extends AbstractValueAdaptingCache {

  private static final String NAME = "NoExpirationCache";
  private final String value;

  public NoExpirationStringCache(String value) {
    super(false);
    this.value = value;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object getNativeCache() {
    return this;
  }

  @Override
  @Nullable
  protected Object lookup(Object key) {
      return value;
  }

  @Override
  public void put(Object key, Object value) {
  }

  @Override
  public void evict(Object key) {
  }

  @Override
  public void clear() {
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Object key, Callable<T> valueLoader) {
    return (T) fromStoreValue(value);
  }
}