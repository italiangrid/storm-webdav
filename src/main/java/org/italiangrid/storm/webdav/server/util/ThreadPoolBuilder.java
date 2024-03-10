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
package org.italiangrid.storm.webdav.server.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;

/**
 * 
 * A builder to support thread pool configuration for a Jetty server.
 *
 */
public class ThreadPoolBuilder {

  /**
   * 
   */
  public static final int DEFAULT_MAX_REQUEST_QUEUE_SIZE = 200;

  public static final int DEFAULT_MAX_THREADS = 50;
  public static final int DEFAULT_MIN_THREADS = 1;

  public static final int DEFAULT_IDLE_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(60);

  public static final String DEFAULT_THREAD_POOL_METRIC_NAME = "thread-pool";
  public static final String DEFAULT_THREAD_POOL_METRIC_PREFIX = "jetty";

  private int maxThreads = DEFAULT_MAX_THREADS;
  private int minThreads = DEFAULT_MIN_THREADS;

  private int idleTimeout = DEFAULT_IDLE_TIMEOUT;
  private int maxRequestQueueSize = DEFAULT_MAX_REQUEST_QUEUE_SIZE;

  private String name = DEFAULT_THREAD_POOL_METRIC_NAME;
  private String prefix = DEFAULT_THREAD_POOL_METRIC_PREFIX;

  private MetricRegistry registry;

  /**
   * Returns a new {@link ThreadPoolBuilder} instance.
   * 
   * @return a {@link ThreadPoolBuilder}
   */
  public static ThreadPoolBuilder instance() {

    return new ThreadPoolBuilder();
  }

  /**
   * Sets the max number of threads for the thread pool.
   * 
   * @param maxNumberOfThreads the max number of threads
   * 
   * @return this builder
   * 
   */
  public ThreadPoolBuilder withMaxThreads(int maxNumberOfThreads) {

    this.maxThreads = maxNumberOfThreads;
    return this;
  }

  /**
   * Sets the minimum number of threads for the thread pool.
   * 
   * @param minNumberOfThreads the minimum number of threads
   * @return this builder
   */
  public ThreadPoolBuilder withMinThreads(int minNumberOfThreads) {

    this.minThreads = minNumberOfThreads;
    return this;
  }

  /**
   * Sets the maximum request queue size for this thread pool.
   * 
   * @param queueSize the maximum request queue size.
   * 
   * @return this builder
   */
  public ThreadPoolBuilder withMaxRequestQueueSize(int queueSize) {

    this.maxRequestQueueSize = queueSize;
    return this;
  }

  /**
   * Sets the registry for this thread pool
   * 
   * @param registry the metric registry
   * @return this builder
   */
  public ThreadPoolBuilder registry(MetricRegistry registry) {
    this.registry = registry;
    return this;
  }

  /**
   * Sets the idle timeout in msec for this thread pool
   * 
   * @param idleTimeout the timeout in milliseconds
   * @return this builder
   */
  public ThreadPoolBuilder withIdleTimeoutMsec(int idleTimeout) {
    this.idleTimeout = idleTimeout;
    return this;
  }

  /**
   * Sets the name for this thread pool
   *
   * @param name the thread pool name
   * @return this builder
   */
  public ThreadPoolBuilder withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets the name prefix for this thread pool
   *
   * @param prefix the thread pool name prefix
   * @return this builder
   */
  public ThreadPoolBuilder withPrefix(String prefix) {
    this.prefix = prefix;
    return this;
  }


  /**
   * ctor.
   * 
   */
  private ThreadPoolBuilder() {

  }

  /**
   * Builds a {@link ThreadPool} based on the parameters of this builder
   * 
   * @return a {@link ThreadPool}
   */
  public ThreadPool build() {

    if (maxRequestQueueSize <= 0) {
      maxRequestQueueSize = DEFAULT_MAX_REQUEST_QUEUE_SIZE;
    }

    if (maxThreads <= 0) {
      maxThreads = DEFAULT_MAX_THREADS;
    }

    if (minThreads <= 0) {
      minThreads = DEFAULT_MIN_THREADS;
    }

    if (idleTimeout <= 0) {
      idleTimeout = DEFAULT_IDLE_TIMEOUT;
    }

    if (prefix.isEmpty()) {
      prefix = DEFAULT_THREAD_POOL_METRIC_PREFIX;
    }

    BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(maxRequestQueueSize);

    QueuedThreadPool tp = null;

    if (registry == null) {
      tp = new QueuedThreadPool(maxThreads, minThreads, idleTimeout, queue);
    } else {
      tp = new InstrumentedQueuedThreadPool(registry, maxThreads, minThreads, idleTimeout, queue, prefix);
    }

    if (name.isEmpty()) {
      name = DEFAULT_THREAD_POOL_METRIC_NAME;
    }
    tp.setName(name);

    return tp;

  }
}
