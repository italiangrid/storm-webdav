// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.scitag;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SciTagTransfer {

  public static final Logger LOG = LoggerFactory.getLogger(SciTagTransfer.class);
  public static final String SCITAG_TRANSFER_ATTRIBUTE = "scitagTransfer";

  private static final String FLOWD_PIPE_NAME = "/var/run/flowd";
  private SciTag scitag;
  private String sourceAddress;
  private int sourcePort;
  private String destinationAddress;
  private int destinationPort;
  private File flowdPipeFile;

  public SciTagTransfer(SciTag scitag, String localAddress, int localPort, String remoteAddress,
      int remotePort) {
    this(scitag, localAddress, localPort, remoteAddress, remotePort, new File(FLOWD_PIPE_NAME));
  }

  public SciTagTransfer(SciTag scitag, String localAddress, int localPort, String remoteAddress,
      int remotePort, File flowdPipeFile) {
    this.scitag = scitag;
    if (scitag.remoteAddressIsSource()) {
      this.sourceAddress = remoteAddress;
      this.sourcePort = remotePort;
      this.destinationAddress = localAddress;
      this.destinationPort = localPort;
    } else {
      this.sourceAddress = localAddress;
      this.sourcePort = localPort;
      this.destinationAddress = remoteAddress;
      this.destinationPort = remotePort;
    }
    this.flowdPipeFile = flowdPipeFile;
  }

  private String flowdEntry() {
    return " tcp " + sourceAddress + " " + sourcePort + " " + destinationAddress + " "
        + destinationPort + " " + scitag.experimentId() + " " + scitag.activityId() + "\n";
  }

  public void writeStart() {
    try (RandomAccessFile flowdPipe = new RandomAccessFile(flowdPipeFile, "rw")) {
      flowdPipe.writeBytes("start" + this.flowdEntry());
    } catch (IOException e) {
      LOG.warn(e.getMessage(), e);
    }
  }

  public void writeEnd() {
    try (RandomAccessFile flowdPipe = new RandomAccessFile(flowdPipeFile, "rw")) {
      flowdPipe.writeBytes("end" + this.flowdEntry());
    } catch (IOException e) {
      LOG.warn(e.getMessage(), e);
    }
  }

}
