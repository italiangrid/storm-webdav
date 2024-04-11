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
import java.net.InetSocketAddress;
import java.net.Socket;
import org.apache.http.HttpHost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.scitag.SciTagTransfer;

public class TpcPlainConnectionSocketFactory extends PlainConnectionSocketFactory {

  public static final TpcPlainConnectionSocketFactory INSTANCE =
      new TpcPlainConnectionSocketFactory();

  public static TpcPlainConnectionSocketFactory getSocketFactory() {
    return INSTANCE;
  }

  public TpcPlainConnectionSocketFactory() {
    super();
  }

  @Override
  public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host,
      InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context)
      throws IOException {
    Socket s =
        super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
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
