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
