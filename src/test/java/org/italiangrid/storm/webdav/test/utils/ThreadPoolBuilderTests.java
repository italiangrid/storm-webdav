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
package org.italiangrid.storm.webdav.test.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.server.util.ThreadPoolBuilder.DEFAULT_IDLE_TIMEOUT;
import static org.italiangrid.storm.webdav.server.util.ThreadPoolBuilder.DEFAULT_MAX_THREADS;
import static org.italiangrid.storm.webdav.server.util.ThreadPoolBuilder.DEFAULT_MIN_THREADS;
import static org.italiangrid.storm.webdav.server.util.ThreadPoolBuilder.DEFAULT_THREAD_POOL_METRIC_NAME;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.italiangrid.storm.webdav.server.util.ThreadPoolBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;

@ExtendWith(MockitoExtension.class)
public class ThreadPoolBuilderTests {

  final static String PREFIX = "iam";
  final static String NAME = "queue";
  final static int IDLE_TIMEOUT = 6;
  final static int MIN_THREADS = 10;
  final static int MAX_THREADS = 20;
  final static int MAX_QUEUE_SIZE = 40;

  @Mock
  MetricRegistry registry;

  @Test
  public void threadPoolBuilderTests() {

    ThreadPool tp = ThreadPoolBuilder.instance()
      .withMinThreads(MIN_THREADS)
      .withMaxThreads(MAX_THREADS)
      .withIdleTimeoutMsec(IDLE_TIMEOUT)
      .withMaxRequestQueueSize(MAX_QUEUE_SIZE)
      .withName(NAME)
      .withPrefix(PREFIX)
      .build();
    assertNotNull(tp);
    assertThat(tp.getClass(), is(QueuedThreadPool.class));
    QueuedThreadPool qtp = (QueuedThreadPool) tp;
    assertThat(qtp.getMinThreads(), is(MIN_THREADS));
    assertThat(qtp.getMaxThreads(), is(MAX_THREADS));
    assertThat(qtp.getIdleTimeout(), is(IDLE_TIMEOUT));
    assertThat(qtp.getMaxAvailableThreads(), is(MAX_THREADS));
    assertThat(qtp.getName(), is(NAME));
  }

  @Test
  public void threadPoolBuilderWithDefaultValuesTests() {

    ThreadPool tp = ThreadPoolBuilder.instance().build();
    assertNotNull(tp);
    assertThat(tp.getClass(), is(QueuedThreadPool.class));
    QueuedThreadPool qtp = (QueuedThreadPool) tp;
    assertThat(qtp.getMinThreads(), is(DEFAULT_MIN_THREADS));
    assertThat(qtp.getMaxThreads(), is(DEFAULT_MAX_THREADS));
    assertThat(qtp.getIdleTimeout(), is(DEFAULT_IDLE_TIMEOUT));
    assertThat(qtp.getMaxAvailableThreads(), is(DEFAULT_MAX_THREADS));
    assertThat(qtp.getName(), is(DEFAULT_THREAD_POOL_METRIC_NAME));
  }

  @Test
  public void threadPoolBuilderWithRegistryTests() {

    ThreadPool tp = ThreadPoolBuilder.instance().registry(registry).build();
    assertNotNull(tp);
    assertThat(tp.getClass(), is(InstrumentedQueuedThreadPool.class));
    InstrumentedQueuedThreadPool qtp = (InstrumentedQueuedThreadPool) tp;
    assertThat(qtp.getMinThreads(), is(DEFAULT_MIN_THREADS));
    assertThat(qtp.getMaxThreads(), is(DEFAULT_MAX_THREADS));
    assertThat(qtp.getIdleTimeout(), is(DEFAULT_IDLE_TIMEOUT));
    assertThat(qtp.getMaxAvailableThreads(), is(DEFAULT_MAX_THREADS));
    assertThat(qtp.getName(), is(DEFAULT_THREAD_POOL_METRIC_NAME));
  }
}