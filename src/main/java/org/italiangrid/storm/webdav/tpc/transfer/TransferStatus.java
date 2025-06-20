// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer;

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

  private TransferStatus(Instant now, Status s, long bc) {
    this.status = s;
    this.transferByteCount = bc;
    this.errorMessage = Optional.empty();
    this.instant = now;
  }

  private TransferStatus(Instant now, String errorMessage) {
    this.status = Status.ERROR;
    this.transferByteCount = 0;
    this.errorMessage = Optional.of(errorMessage);
    this.instant = now;
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
          .append("Total Stripe Count: 1\nEnd\n");
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

    Clock clock;

    private Builder(Clock c) {
      this.clock = c;
    }

    public Builder withClock(Clock c) {
      this.clock = c;
      return this;
    }

    public TransferStatus inProgress(long byteCount) {
      return new TransferStatus(clock.instant(), Status.STARTED, byteCount);
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
