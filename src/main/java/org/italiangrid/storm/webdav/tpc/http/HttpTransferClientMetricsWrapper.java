/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class HttpTransferClientMetricsWrapper implements TransferClient {

  final MetricRegistry registry;
  final TransferClient delegate;

  final Timer pullTimer;
  final Timer pushTimer;

  public HttpTransferClientMetricsWrapper(MetricRegistry registry, TransferClient delegate) {
    this.registry = registry;
    this.delegate = delegate;
    pullTimer = registry.timer(name(TransferClient.class, "pull"));
    pushTimer = registry.timer(name(TransferClient.class, "push"));
  }

  @Override
  public void handle(GetTransferRequest request, TransferStatusCallback status)
      throws ClientProtocolException {

    final Timer.Context context = pullTimer.time();

    try {
      delegate.handle(request, status);
    } finally {
      context.stop();
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
    }
  }

}
