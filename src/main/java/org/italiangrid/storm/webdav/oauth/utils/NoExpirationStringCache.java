// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
    // Nothing to do
  }

  @Override
  public void evict(Object key) {
    // Nothing to do
  }

  @Override
  public void clear() {
    // Nothing to do
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Object key, Callable<T> valueLoader) {
    return (T) fromStoreValue(value);
  }
}
