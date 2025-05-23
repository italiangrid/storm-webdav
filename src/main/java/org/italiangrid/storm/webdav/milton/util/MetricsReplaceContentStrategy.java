// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton.util;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MetricsReplaceContentStrategy implements ReplaceContentStrategy {

  final Timer timer;
  final ReplaceContentStrategy delegate;

  public MetricsReplaceContentStrategy(MetricRegistry registry, ReplaceContentStrategy delegate) {
    timer = registry.timer(name("storm.checksum-strategy"));
    this.delegate = delegate;
  }

  @Override
  public void replaceContent(InputStream in, Long length, File targetFile) throws IOException {
    Timer.Context context = timer.time();

    try {
      delegate.replaceContent(in, length, targetFile);
    } finally {
      context.stop();
    }
  }
}
