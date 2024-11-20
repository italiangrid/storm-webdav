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
