// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.transfer;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import com.google.common.collect.Multimap;

import org.italiangrid.storm.webdav.scitag.SciTag;

public interface TransferRequest {

  String uuid();

  String path();

  URI remoteURI();

  Multimap<String, String> transferHeaders();

  SciTag scitag();

  boolean verifyChecksum();

  boolean overwrite();

  Optional<TransferStatus> lastTransferStatus();

  void setTransferStatus(TransferStatus status);

  String statusString();

  long bytesTransferred();

  Duration duration();

  Optional<Double> transferThroughputBytesPerSec();

  Instant startTime();

  Instant endTime();

  boolean endedSuccesfully();

  boolean endedInError();

}
