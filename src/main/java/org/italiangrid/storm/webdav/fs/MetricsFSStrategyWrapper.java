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
package org.italiangrid.storm.webdav.fs;

import static com.codahale.metrics.MetricRegistry.name;

import java.io.File;
import java.io.InputStream;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class MetricsFSStrategyWrapper implements FilesystemAccess {
  public static final String METRIC_NAME = "fs";

  final FilesystemAccess delegate;
  final MetricRegistry registry;

  final Timer mkdirTimer;
  final Timer rmTimer;
  final Timer mvTimer;
  final Timer cpTimer;
  final Timer lsTimer;
  final Timer createTimer;

  public MetricsFSStrategyWrapper(FilesystemAccess delegate,
    MetricRegistry registry) {

    this.delegate = delegate;
    this.registry = registry;

    mkdirTimer = registry.timer(name(METRIC_NAME, "mkdir"));
    rmTimer = registry.timer(name(METRIC_NAME, "rm"));
    cpTimer = registry.timer(name(METRIC_NAME, "cp"));
    lsTimer = registry.timer(name(METRIC_NAME, "ls"));
    mvTimer = registry.timer(name(METRIC_NAME, "mv"));
    createTimer = registry.timer(name(METRIC_NAME, "create"));

  }

  @Override
  public File mkdir(File parentDirectory, String dirName) {

    final Timer.Context context = mkdirTimer.time();

    try {

      return delegate.mkdir(parentDirectory, dirName);

    } finally {

      context.stop();
    }

  }

  @Override
  public boolean rm(File f) {

    final Timer.Context context = rmTimer.time();

    try {
      return delegate.rm(f);

    } finally {
      context.stop();
    }

  }

  @Override
  public void mv(File source, File dest) {

    final Timer.Context context = mvTimer.time();

    try {
      delegate.mv(source, dest);

    } finally {
      context.stop();
    }

  }

  @Override
  public void cp(File source, File dest) {

    final Timer.Context context = cpTimer.time();
    try {

      delegate.cp(source, dest);

    } finally {
      context.stop();
    }

  }

  @Override
  public File[] ls(File dir, int limit) {

    final Timer.Context context = lsTimer.time();
    try {
      return delegate.ls(dir, limit);
    } finally {

      context.stop();
    }
  }

  @Override
  public File create(File file, InputStream in) {

    final Timer.Context context = createTimer.time();
    try {
      return delegate.create(file, in);
    } finally {

      context.stop();
    }

  }

}
