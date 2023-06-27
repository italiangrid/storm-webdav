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
package org.italiangrid.storm.webdav.milton.util;

import static com.codahale.metrics.MetricRegistry.name;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

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
