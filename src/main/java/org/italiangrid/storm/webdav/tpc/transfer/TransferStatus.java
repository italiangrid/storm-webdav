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
package org.italiangrid.storm.webdav.tpc.transfer;

import static java.lang.String.format;
import static org.italiangrid.storm.webdav.tpc.transfer.TransferStatus.Status.DONE;
import static org.italiangrid.storm.webdav.tpc.transfer.TransferStatus.Status.ERROR;

import java.time.Instant;
import java.util.Optional;

public class TransferStatus {

  public enum Status {
    STARTED,
    ERROR,
    DONE
  }

  final Status status;
  final long transferByteCount;
  final Optional<String> errorMessage;
  final long epochSecond;

  private TransferStatus(Status s, long bc) {
    this.status = s;
    this.transferByteCount = bc;
    this.errorMessage = Optional.empty();
    this.epochSecond = Instant.now().getEpochSecond();
  }

  private TransferStatus(String errorMessage) {
    this.status = Status.ERROR;
    this.transferByteCount = 0;
    this.errorMessage = Optional.of(errorMessage);
    this.epochSecond = Instant.now().getEpochSecond();
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

  public static TransferStatus inProgress(long byteCount) {
    return new TransferStatus(Status.STARTED, byteCount);
  }

  public static TransferStatus done(long byteCount) {
    return new TransferStatus(Status.DONE, byteCount);
  }

  public static TransferStatus error(String errorMessage) {
    return new TransferStatus(errorMessage);
  }

  public String asPerfMarker() {

    StringBuilder builder = new StringBuilder();

    if (status == DONE) {
      builder.append("success: Created");
    } else if (status == ERROR) {
      builder.append(String.format("failure: %s", getErrorMessage().orElse("")));
    } else {
      builder.append("Perf Marker\n");
      builder.append(format("Timestamp: %d\n", epochSecond));
      builder.append("Stripe Index: 0\n");
      builder.append(format("Stripe Bytes Transferred: %d\n", getTransferByteCount()));
      builder.append("Total Stripe Count: 1\n");
      builder.append("End\n");
    }

    return builder.toString();
  }
  
  @Override
  public String toString() {
   
    StringBuilder builder  = new StringBuilder();
   builder.append(status);
   if (errorMessage.isPresent()) {
     builder.append(" : ");
     builder.append(errorMessage.get());
   }
   
   return builder.toString();
  }
  
  public long epochSecond() {
    return epochSecond;
  }
}
