// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.tpc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequestBuilder;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferStatsTest {

  public static final Instant NOW = Instant.parse("2021-01-01T00:00:00.00Z");

  Clock clock = Clock.fixed(NOW, ZoneId.systemDefault());

  TransferStatus.Builder status = TransferStatus.builder(clock);

  @Mock Socket socket;

  @Test
  void testByteCountPull() {

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
  void testOneMsecByteCountPull() {

    GetTransferRequest req = GetTransferRequestBuilder.create().build();
    req.setTransferStatus(status.inProgress(0));
    status.withClock(Clock.offset(clock, Duration.ofMillis(1)));
    req.setTransferStatus(status.done(1000));

    assertThat(req.bytesTransferred(), is(1000L));
    assertThat(req.duration().toMillis(), is(1L));

    assertThat(req.transferThroughputBytesPerSec().get(), is(1000000.0));
  }

  @Test
  void testHalfMsecByteCountPull() {

    GetTransferRequest req = GetTransferRequestBuilder.create().build();
    req.setTransferStatus(status.inProgress(0));
    status.withClock(Clock.offset(clock, Duration.ofNanos(1000)));
    req.setTransferStatus(status.done(1000));

    assertThat(req.bytesTransferred(), is(1000L));
    assertThat(req.duration().toMillis(), is(0L));

    assertThat(req.transferThroughputBytesPerSec().get(), is(1000000.0));
  }

  @Test
  void testDonePerfMarker() {
    assertThat(status.done(0).asPerfMarker(), containsString("success: Created"));
  }

  @Test
  void testInProgressPerfMarkerIPv4() throws UnknownHostException {
    when(socket.getLocalAddress()).thenReturn(InetAddress.getByName("10.10.10.1"));
    when(socket.getLocalPort()).thenReturn(12345);
    when(socket.getInetAddress()).thenReturn(InetAddress.getByName("10.10.10.2"));
    when(socket.getPort()).thenReturn(8443);
    status.withSocket(socket);

    status.withIsPushMode(false);
    String perfMarkerPullMode = status.inProgress(0).asPerfMarker();
    assertThat(perfMarkerPullMode, containsString("RemoteConnections: tcp:10.10.10.2:8443"));
    assertThat(
        perfMarkerPullMode, containsString("Connection: tcp:10.10.10.2:8443:10.10.10.1:12345"));

    status.withIsPushMode(true);
    String perfMarkerPushMode = status.inProgress(0).asPerfMarker();
    assertThat(perfMarkerPushMode, containsString("RemoteConnections: tcp:10.10.10.2:8443"));
    assertThat(
        perfMarkerPushMode, containsString("Connection: tcp:10.10.10.1:12345:10.10.10.2:8443"));
  }

  @Test
  void testInProgressPerfMarkerIPv6() throws UnknownHostException {
    when(socket.getLocalAddress()).thenReturn(InetAddress.getByName("fc00::1"));
    when(socket.getLocalPort()).thenReturn(12345);
    when(socket.getInetAddress()).thenReturn(InetAddress.getByName("fc00::2"));
    when(socket.getPort()).thenReturn(8443);
    status.withSocket(socket);

    status.withIsPushMode(false);
    String perfMarkerPullMode = status.inProgress(0).asPerfMarker();
    assertThat(
        perfMarkerPullMode, containsString("RemoteConnections: tcp:[fc00:0:0:0:0:0:0:2]:8443"));
    assertThat(
        perfMarkerPullMode,
        containsString("Connection: tcp:[fc00:0:0:0:0:0:0:2]:8443:[fc00:0:0:0:0:0:0:1]:12345"));

    status.withIsPushMode(true);
    String perfMarkerPushMode = status.inProgress(0).asPerfMarker();
    assertThat(
        perfMarkerPushMode, containsString("RemoteConnections: tcp:[fc00:0:0:0:0:0:0:2]:8443"));
    assertThat(
        perfMarkerPushMode,
        containsString("Connection: tcp:[fc00:0:0:0:0:0:0:1]:12345:[fc00:0:0:0:0:0:0:2]:8443"));
  }
}
