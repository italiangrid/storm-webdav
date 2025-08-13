// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton.util;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MetricsReplaceContentStrategy implements ReplaceContentStrategy {

  final ReplaceContentStrategy delegate;
  private final ObservationRegistry observationRegistry;

  public MetricsReplaceContentStrategy(
      ReplaceContentStrategy delegate, ObservationRegistry observationRegistry) {
    this.delegate = delegate;
    this.observationRegistry = observationRegistry;
  }

  @Override
  public void replaceContent(InputStream in, Long length, File targetFile) throws IOException {
    Observation observation =
        Observation.createNotStarted("replace-content", this.observationRegistry);
    observation.observeChecked(
        () -> {
          delegate.replaceContent(in, length, targetFile);
        });
  }
}
