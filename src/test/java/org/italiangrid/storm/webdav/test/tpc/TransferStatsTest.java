/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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
package org.italiangrid.storm.webdav.test.tpc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequestBuilder;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransferStatsTest {

  public static final Instant NOW = Instant.parse("2021-01-01T00:00:00.00Z");

  Clock clock = Clock.fixed(NOW, ZoneId.systemDefault());

  TransferStatus.Builder status = TransferStatus.builder(clock);

  @Test
  public void testByteCountPull() {

    GetTransferRequest req = GetTransferRequestBuilder.create().build();

    req.setTransferStatus(status.inProgress(0));
    status.withClock(Clock.offset(clock, Duration.ofSeconds(1)));
    req.setTransferStatus(status.inProgress(900));
    status.withClock(Clock.offset(clock, Duration.ofSeconds(2)));
    req.setTransferStatus(status.done(1000));

    assertThat(req.bytesTransferred(), is(1000L));
    assertThat(req.duration().getSeconds(), is(2L));
    assertThat(req.transferThroughputBytesPerSec().get(), is(500.0));

  }

  @Test
  public void testOneMsecByteCountPull() {

    GetTransferRequest req = GetTransferRequestBuilder.create().build();
    req.setTransferStatus(status.inProgress(0));
    status.withClock(Clock.offset(clock, Duration.ofMillis(1)));
    req.setTransferStatus(status.done(1000));

    assertThat(req.bytesTransferred(), is(1000L));
    assertThat(req.duration().toMillis(), is(1L));

    assertThat(req.transferThroughputBytesPerSec().get(), is(1000000.0));

  }

  @Test
  public void testHalfMsecByteCountPull() {

    GetTransferRequest req = GetTransferRequestBuilder.create().build();
    req.setTransferStatus(status.inProgress(0));
    status.withClock(Clock.offset(clock, Duration.ofNanos(1000)));
    req.setTransferStatus(status.done(1000));

    assertThat(req.bytesTransferred(), is(1000L));
    assertThat(req.duration().toMillis(), is(0L));

    assertThat(req.transferThroughputBytesPerSec().get(), is(1000000.0));

  }



}
