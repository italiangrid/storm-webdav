// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer.impl;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.tpc.transfer.TransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

public abstract class TransferRequestImpl implements TransferRequest {
  public static final Logger LOG = LoggerFactory.getLogger(TransferRequestImpl.class);

  final String uuid;

  final String path;

  final URI uri;

  final Multimap<String, String> xferHeaders;

  final SciTag scitag;

  final boolean verifyChecksum;

  final boolean overwrite;

  Instant startTime;

  Instant endTime;

  private Optional<TransferStatus> lastTransferStatus = Optional.empty();

  TransferRequestImpl(String uuid, String path, URI uri, Multimap<String, String> xferHeaders,
      SciTag scitag, boolean verifyChecksum, boolean overwrite) {

    this.uuid = uuid;
    this.path = path;
    this.uri = uri;
    this.xferHeaders = xferHeaders;
    this.scitag = scitag;
    this.verifyChecksum = verifyChecksum;
    this.overwrite = overwrite;
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public URI remoteURI() {
    return uri;
  }

  @Override
  public Multimap<String, String> transferHeaders() {
    return xferHeaders;
  }

  @Override
  public SciTag scitag() {
    return scitag;
  }

  @Override
  public boolean verifyChecksum() {
    return verifyChecksum;
  }

  @Override
  public boolean overwrite() {
    return overwrite;
  }

  @Override
  public void setTransferStatus(TransferStatus status) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("TPC id: {}, status: {}", uuid, status);
    }

    if (!lastTransferStatus.isPresent()) {
      startTime = status.getInstant();
    }

    this.lastTransferStatus = Optional.of(status);

    if (TransferStatus.Status.ERROR.equals(status.getStatus())
        || TransferStatus.Status.DONE.equals(status.getStatus())) {
      endTime = status.getInstant();
    }
  }

  @Override
  public Optional<TransferStatus> lastTransferStatus() {
    return lastTransferStatus;
  }

  @Override
  public String uuid() {
    return uuid;
  }

  @Override
  public Optional<Double> transferThroughputBytesPerSec() {
    if (!lastTransferStatus.isPresent()) {
      return Optional.empty();
    }

    if (!lastTransferStatus.get().getStatus().equals(TransferStatus.Status.DONE)) {
      return Optional.empty();
    }

    TransferStatus lastStatus = lastTransferStatus.get();

    // Return an empty throughput in case the last successful transfer is older than 10 seconds
    Duration res = Duration.between(Instant.now(), lastTransferStatus.get().getInstant());
    if (res.getSeconds() > 10) {
      return Optional.empty();
    }

    Duration xferDuration = Duration.between(startTime, endTime).abs();

    if (xferDuration.isZero() || xferDuration.toMillis() == 0) {
      return Optional.of((double) lastStatus.getTransferByteCount() * 1000);
    }

    double bytesPerSecond =
        ((double) lastStatus.getTransferByteCount() / xferDuration.toMillis()) * 1000;

    return Optional.of(bytesPerSecond);
  }

  @Override
  public long bytesTransferred() {
    if (!lastTransferStatus.isPresent()) {
      return 0;
    }
    return lastTransferStatus.get().getTransferByteCount();
  }


  @Override
  public Duration duration() {

    if (startTime == null || endTime == null) {
      LOG.debug("Duration called before end of trasnfer, will return ZERO");
      return Duration.ZERO;
    }

    return Duration.between(startTime, endTime);
  }

  @Override
  public Instant startTime() {
    return startTime;
  }

  @Override
  public Instant endTime() {
    return endTime;
  }

  @Override
  public boolean endedSuccesfully() {

    return lastTransferStatus.isPresent()
        && lastTransferStatus.get().getStatus().equals(Status.DONE);

  }

  @Override
  public boolean endedInError() {
    return lastTransferStatus.isPresent()
        && lastTransferStatus.get().getStatus().equals(Status.ERROR);
  }
}
