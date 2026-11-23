// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.scitag;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SciTagTransfer {

  public static final Logger LOG = LoggerFactory.getLogger(SciTagTransfer.class);
  public static final String SCITAG_TRANSFER_ATTRIBUTE = "scitagTransfer";

  private static final String FLOWD_PIPE_NAME = "/var/run/flowd";
  private final SciTag scitag;
  private final String sourceAddress;
  private final int sourcePort;
  private final String destinationAddress;
  private final int destinationPort;
  private final Path flowdPipePath;

  public SciTagTransfer(
      SciTag scitag, String localAddress, int localPort, String remoteAddress, int remotePort) {
    this(scitag, localAddress, localPort, remoteAddress, remotePort, Path.of(FLOWD_PIPE_NAME));
  }

  public SciTagTransfer(
      SciTag scitag,
      String localAddress,
      int localPort,
      String remoteAddress,
      int remotePort,
      Path flowdPipePath) {
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
    this.flowdPipePath = flowdPipePath;
  }

  private void writeFlowdEntry(String state) {
    String flowdEntry =
        state
            + " tcp "
            + sourceAddress
            + " "
            + sourcePort
            + " "
            + destinationAddress
            + " "
            + destinationPort
            + " "
            + scitag.experimentId()
            + " "
            + scitag.activityId()
            + "\n";
    try {
      // Intentionally open the FIFO in write-only mode. If flowd is not running, block immediately
      // rather than silently buffering events until the pipe becomes full.
      Files.writeString(flowdPipePath, flowdEntry, StandardOpenOption.WRITE);
    } catch (IOException e) {
      LOG.warn(e.getMessage(), e);
    }
  }

  public void writeStart() {
    writeFlowdEntry("start");
  }

  public void writeEnd() {
    writeFlowdEntry("end");
  }

  public static SciTagTransfer fromHttpServletRequest(HttpServletRequest request, SciTag scitag) {
    return fromHttpServletRequest(request, scitag, Path.of(FLOWD_PIPE_NAME));
  }

  public static SciTagTransfer fromHttpServletRequest(
      HttpServletRequest request, SciTag scitag, Path flowdPipePath) {
    SciTagTransfer scitagTransfer = null;
    if (scitag != null) {
      try {
        scitagTransfer =
            new SciTagTransfer(
                scitag,
                InetAddress.getByName(request.getLocalAddr()).getHostAddress(),
                request.getLocalPort(),
                InetAddress.getByName(request.getRemoteAddr()).getHostAddress(),
                request.getRemotePort(),
                flowdPipePath);
      } catch (UnknownHostException e) {
        LOG.info("Invalid address for SciTag", e);
      }
    }
    return scitagTransfer;
  }
}
