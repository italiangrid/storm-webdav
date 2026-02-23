// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton.util;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MetricsReplaceContentStrategy implements ReplaceContentStrategy {

  final Timer timer;
  final ReplaceContentStrategy delegate;
  private final ObservationRegistry observationRegistry;

  public MetricsReplaceContentStrategy(
      MetricRegistry registry,
      ReplaceContentStrategy delegate,
      ObservationRegistry observationRegistry) {
    timer = registry.timer(name("storm.checksum-strategy"));
    this.delegate = delegate;
    this.observationRegistry = observationRegistry;
  }

  @Override
  public void replaceContent(InputStream in, Long length, File targetFile) throws IOException {
    Observation observation =
        Observation.createNotStarted("replace-content", this.observationRegistry);
    observation.observeChecked(
        () -> {
          Timer.Context context = timer.time();

          try {
            delegate.replaceContent(in, length, targetFile);
          } finally {
            context.stop();
          }
        });
  }
}
