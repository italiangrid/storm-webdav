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
package org.italiangrid.storm.webdav.milton;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.AuthenticationHandler;
import io.milton.http.Handler;
import io.milton.http.http11.DefaultHttp11ResponseHandler.BUFFERING;
import io.milton.http.webdav.MoveHandler;

import java.util.List;

public class StoRMHTTPManagerBuilder extends HttpManagerBuilder {

  public StoRMHTTPManagerBuilder() {

    setDefaultStandardFilter(new StoRMMiltonBehaviour());
    setEnabledJson(false);

    setBuffering(BUFFERING.never);
    setEnableCompression(false);

    List<AuthenticationHandler> authHandlers =
        List.of((AuthenticationHandler) new NullAuthenticationHandler());

    setAuthenticationHandlers(authHandlers);

    setEnableBasicAuth(false);
    setEnableExpectContinue(false);
    setEnableFormAuth(false);
    setEnableCookieAuth(false);
    setEnableDigestAuth(false);

  }

  @Override
  protected void afterInit() {

    super.afterInit();
    disableDeleteExistingBeforeMoveInMoveHandler();
  }


  private void disableDeleteExistingBeforeMoveInMoveHandler() {

    for (Handler h : getWebDavProtocol().getHandlers()) {
      if (h instanceof MoveHandler moveHandler) {
        moveHandler.setDeleteExistingBeforeMove(false);
      }
    }
  }
}
