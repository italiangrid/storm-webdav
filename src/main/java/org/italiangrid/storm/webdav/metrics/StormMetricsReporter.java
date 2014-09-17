/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
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
package org.italiangrid.storm.webdav.metrics;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

public class StormMetricsReporter extends ScheduledReporter {

  public static final String METRICS_LOGGER_NAME = "storm-metrics-logger";
  
  private static final Logger logger = LoggerFactory
    .getLogger(METRICS_LOGGER_NAME);
  
  private Long lastCountValue = null;
  
  private StormMetricsReporter(MetricRegistry registry, MetricFilter filter,
    TimeUnit rateUnit, TimeUnit durationUnit) {

    super(registry, "storm", filter, rateUnit,
      durationUnit);
  }

  public static Builder forRegistry(MetricRegistry registry) {

    return new Builder(registry);
  }

  public static class Builder {

    private final MetricRegistry registry;
    private MetricFilter filter;
    private TimeUnit rateUnit;
    private TimeUnit durationUnit;

    private Builder(MetricRegistry r) {

      this.registry = r;
      filter = MetricFilter.ALL;
      rateUnit = TimeUnit.SECONDS;
      durationUnit = TimeUnit.MILLISECONDS;
    }

    public Builder filter(MetricFilter filter) {

      this.filter = filter;
      return this;
    }

    public Builder rateUnit(TimeUnit unit) {

      this.rateUnit = unit;
      return this;
    }

    public Builder durationUnit(TimeUnit unit) {

      this.durationUnit = unit;
      return this;
    }

    public StormMetricsReporter build() {

      return new StormMetricsReporter(registry, filter, rateUnit, durationUnit);

    }
  }

  public StormMetricsReporter(MetricRegistry registry, String name,
    MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {

    super(registry, name, filter, rateUnit, durationUnit);

  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void report(SortedMap<String, Gauge> gauges,
    SortedMap<String, Counter> counters,
    SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
    SortedMap<String, Timer> timers) {

    Gauge<Double> heapUsage = gauges.get("jvm.memory.heap.usage");
    Gauge<Long> heapUsed = gauges.get("jvm.memory.heap.used");

    Timer handlerDispatches = timers.get("storm.http.handler.dispatches");
    final Snapshot snapshot = handlerDispatches.getSnapshot();
    
    Long lastMinuteCount = null;
    
    if (lastCountValue != null ){
      lastMinuteCount = handlerDispatches.getCount() - lastCountValue;
    }
    
    lastCountValue = handlerDispatches.getCount();
    
    logger
      .info(
        "Heap[usage={}, used={}] Requests[m1_count={}, count={}, max={}, min={}, mean={}, mean_rate={}, m1_rate={}, m5_rate={}, m15_rate={}] Duration_units={}, Rate_units={}",
        heapUsage.getValue(),
        FileUtils.byteCountToDisplaySize(heapUsed.getValue()),
        lastMinuteCount,
        handlerDispatches.getCount(), 
        convertDuration(snapshot.getMax()),
        convertDuration(snapshot.getMin()),
        convertDuration(snapshot.getMean()),
        convertRate(handlerDispatches.getMeanRate()),
        convertRate(handlerDispatches.getOneMinuteRate()),
        convertRate(handlerDispatches.getFiveMinuteRate()),
        convertRate(handlerDispatches.getFifteenMinuteRate()), 
        getDurationUnit(),
        getRateUnit());

  }
  
  public String getRateUnit(){
    return "events/" + super.getRateUnit();
  }

}
