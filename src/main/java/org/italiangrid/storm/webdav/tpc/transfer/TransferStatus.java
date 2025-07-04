// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer;

import java.net.Inet6Address;
import java.net.Socket;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

public final class TransferStatus {

  public enum Status {
    STARTED,
    ERROR,
    DONE
  }

  final Status status;
  final long transferByteCount;
  final Optional<String> errorMessage;
  final Instant instant;
  final String localIp;
  final String remoteIp;
  final boolean isPushMode;

  private TransferStatus(Instant now, Status s, long bc) {
    this(now, s, bc, null, null, false);
  }

  private TransferStatus(
      Instant now, Status s, long bc, String localIp, String remoteIp, boolean isPushMode) {
    this.status = s;
    this.transferByteCount = bc;
    this.errorMessage = Optional.empty();
    this.instant = now;
    this.localIp = localIp;
    this.remoteIp = remoteIp;
    this.isPushMode = isPushMode;
  }

  private TransferStatus(Instant now, String errorMessage) {
    this.status = Status.ERROR;
    this.transferByteCount = 0;
    this.errorMessage = Optional.of(errorMessage);
    this.instant = now;
    this.localIp = null;
    this.remoteIp = null;
    this.isPushMode = false;
  }

  public Status getStatus() {
    return status;
  }

  public long getTransferByteCount() {
    return transferByteCount;
  }

  public Optional<String> getErrorMessage() {
    return errorMessage;
  }

  public String asPerfMarker() {

    StringBuilder builder = new StringBuilder();

    if (status == Status.DONE) {
      builder.append("success: Created");
    } else if (status == Status.ERROR) {
      builder.append(String.format("failure: %s", getErrorMessage().orElse("")));
    } else {
      builder
          .append("Perf Marker\n")
          .append(String.format("Timestamp: %d%n", instant.getEpochSecond()))
          .append("Stripe Index: 0\n")
          .append(String.format("Stripe Bytes Transferred: %d%n", getTransferByteCount()))
          .append("Total Stripe Count: 1\n");
      if (remoteIp != null) {
        builder.append("RemoteConnections: tcp:").append(remoteIp).append("\n");
        if (localIp != null) {
          builder.append("Connection: tcp:");
          if (isPushMode) {
            builder.append(localIp).append(":").append(remoteIp);
          } else {
            builder.append(remoteIp).append(":").append(localIp);
          }
          builder.append("\n");
        }
      }
      builder.append("End\n");
    }

    return builder.toString();
  }

  @Override
  public String toString() {

    StringBuilder builder = new StringBuilder();
    builder.append(status);
    if (errorMessage.isPresent()) {
      builder.append(" : ").append(errorMessage.get());
    }

    return builder.toString();
  }

  public long epochSecond() {
    return instant.getEpochSecond();
  }

  public Instant getInstant() {
    return instant;
  }

  public static final class Builder {

    public static final String TRANSFER_STATUS_BUILDER_ATTRIBUTE = "transferStatusBuilder";

    Clock clock;
    boolean isPushMode;
    String localIp;
    String remoteIp;

    private Builder(Clock c) {
      this.clock = c;
    }

    public Builder withClock(Clock c) {
      this.clock = c;
      return this;
    }

    public Builder withIsPushMode(boolean isPushMode) {
      this.isPushMode = isPushMode;
      return this;
    }

    public Builder withSocket(Socket socket) {
      String localIpFormat = "%s:%d";
      if (socket.getLocalAddress() instanceof Inet6Address) {
        localIpFormat = "[%s]:%d";
      }
      this.localIp =
          String.format(
              localIpFormat, socket.getLocalAddress().getHostAddress(), socket.getLocalPort());
      String remoteIpFormat = "%s:%d";
      if (socket.getInetAddress() instanceof Inet6Address) {
        remoteIpFormat = "[%s]:%d";
      }
      this.remoteIp =
          String.format(remoteIpFormat, socket.getInetAddress().getHostAddress(), socket.getPort());
      return this;
    }

    public TransferStatus inProgress(long byteCount) {
      return new TransferStatus(
          clock.instant(), Status.STARTED, byteCount, localIp, remoteIp, isPushMode);
    }

    public TransferStatus done(long byteCount) {
      return new TransferStatus(clock.instant(), Status.DONE, byteCount);
    }

    public TransferStatus error(String errorMessage) {
      return new TransferStatus(clock.instant(), errorMessage);
    }
  }

  public static Builder builder(Clock clock) {
    return new Builder(clock);
  }
}
