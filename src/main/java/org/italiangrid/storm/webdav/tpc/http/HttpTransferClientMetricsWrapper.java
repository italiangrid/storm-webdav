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
package org.italiangrid.storm.webdav.tpc.http;

import static com.codahale.metrics.MetricRegistry.name;

import org.apache.http.client.ClientProtocolException;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatusCallback;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class HttpTransferClientMetricsWrapper implements TransferClient {

  static final String METRIC_NAME = "TPC";

  final MetricRegistry registry;
  final TransferClient delegate;

  final Timer pullTimer;
  final Timer pushTimer;

  final Meter pullSuccesses;
  final Meter pullErrors;

  final Meter pushSuccesses;
  final Meter pushErrors;

  final Histogram pullThroughput;
  final Histogram pushThroughput;

  public HttpTransferClientMetricsWrapper(MetricRegistry registry, TransferClient delegate) {
    this.registry = registry;
    this.delegate = delegate;
    pullTimer = registry.timer(name(METRIC_NAME, "pull.time"));
    pushTimer = registry.timer(name(METRIC_NAME, "push.time"));

    pullThroughput = registry.histogram(name(METRIC_NAME, "pull.throughput-bytes-per-sec"));
    pushThroughput = registry.histogram(name(METRIC_NAME, "push.throughput-bytes-per-sec"));

    pullSuccesses = registry.meter(name(METRIC_NAME, "pull.ok-count"));
    pullErrors = registry.meter(name(METRIC_NAME, "pull.error-count"));

    pushSuccesses = registry.meter(name(METRIC_NAME, "push.ok-count"));
    pushErrors = registry.meter(name(METRIC_NAME, "push.error-count"));

  }

  private void updateRequestOutcome(GetTransferRequest request) {
    if (request.endedSuccesfully()) {
      pullSuccesses.mark();
    } else if (request.endedInError()) {
      pullErrors.mark();
    }
  }

  private void updateRequestOutcome(PutTransferRequest request) {

    if (request.endedSuccesfully()) {
      pushSuccesses.mark();
    } else if (request.endedInError()) {
      pushErrors.mark();
    }
  }

  private void updateThroughput(GetTransferRequest request) {

    if (request.endedSuccesfully() && request.transferThroughputBytesPerSec().isPresent()) {
      pullThroughput.update(request.transferThroughputBytesPerSec().get().longValue());
    }

  }

  private void updateThroughput(PutTransferRequest request) {

    if (request.endedSuccesfully() && request.transferThroughputBytesPerSec().isPresent()) {
      pushThroughput.update(request.transferThroughputBytesPerSec().get().longValue());
    }
  }

  private void updateStats(GetTransferRequest request, TransferStatusCallback status) {
    updateRequestOutcome(request);
    updateThroughput(request);
  }

  private void updateStats(PutTransferRequest request, TransferStatusCallback status) {
    updateRequestOutcome(request);
    updateThroughput(request);
  }

  @Override
  public void handle(GetTransferRequest request, TransferStatusCallback status)
      throws ClientProtocolException {

    final Timer.Context context = pullTimer.time();

    try {
      delegate.handle(request, status);
    } finally {
      context.stop();
      updateStats(request, status);
    }

  }

  @Override
  public void handle(PutTransferRequest request, TransferStatusCallback status)
      throws ClientProtocolException {

    final Timer.Context context = pushTimer.time();

    try {
      delegate.handle(request, status);
    } finally {
      context.stop();
      updateStats(request, status);
    }
  }

}
