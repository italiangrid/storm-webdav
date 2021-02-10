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



}
