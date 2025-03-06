// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.http;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.hc.client5.http.ClientProtocolException;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatusCallback;

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
    if (request.endedSuccesfully()) {
      request
          .transferThroughputBytesPerSec()
          .ifPresent(
              transferThroughputBytesPerSec ->
                  pullThroughput.update(transferThroughputBytesPerSec.longValue()));
    }
  }

  private void updateThroughput(PutTransferRequest request) {
    if (request.endedSuccesfully()) {
      request
          .transferThroughputBytesPerSec()
          .ifPresent(
              transferThroughputBytesPerSec ->
                  pushThroughput.update(transferThroughputBytesPerSec.longValue()));
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
