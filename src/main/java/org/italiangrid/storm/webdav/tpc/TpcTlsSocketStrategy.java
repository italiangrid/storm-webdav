// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.scitag.SciTagTransfer;

public class TpcTlsSocketStrategy extends DefaultClientTlsStrategy {

  public TpcTlsSocketStrategy(SSLContext sslContext) {
    super(sslContext);
  }

  @Override
  public SSLSocket upgrade(Socket socket, String target, int port, Object attachment, HttpContext context)
      throws IOException {
    SSLSocket s = super.upgrade(socket, target, port, attachment, context);
    SciTag scitag = (SciTag) context.getAttribute(SciTag.SCITAG_ATTRIBUTE);
    if (scitag != null) {
      SciTagTransfer scitagTransfer =
          new SciTagTransfer(scitag, s.getLocalAddress().getHostAddress(), s.getLocalPort(),
              s.getInetAddress().getHostAddress(), s.getPort());
      scitagTransfer.writeStart();
      context.setAttribute(SciTagTransfer.SCITAG_TRANSFER_ATTRIBUTE, scitagTransfer);
    }
    return s;
  }
}
