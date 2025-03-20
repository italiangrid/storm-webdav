// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.http;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpComponentsMetrics {

  public static final Logger LOG = LoggerFactory.getLogger(HttpComponentsMetrics.class);

  private final DistributionSummary bytesIn;
  private final DistributionSummary bytesOut;

  public HttpComponentsMetrics(MeterRegistry registry) {
    this.bytesIn =
        DistributionSummary.builder("httpcomponents.httpclient.bytes.in")
            .baseUnit(BaseUnits.BYTES)
            .description("Bytes received by tracked connections")
            .register(registry);

    this.bytesOut =
        DistributionSummary.builder("httpcomponents.httpclient.bytes.out")
            .baseUnit(BaseUnits.BYTES)
            .description("Bytes sent by tracked connections")
            .register(registry);
  }

  public void incoming(double amount) {
    if (amount > 0) {
      bytesIn.record(amount);
    }
  }

  public void outgoing(double amount) {
    if (amount > 0) {
      bytesOut.record(amount);
    }
  }
}
