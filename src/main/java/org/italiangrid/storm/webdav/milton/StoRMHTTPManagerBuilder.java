// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
    //disableDeleteExistingBeforeMoveInMoveHandler();
  }

  private void disableDeleteExistingBeforeMoveInMoveHandler() {

    for (Handler h : getWebDavProtocol().getHandlers()) {
      if (h instanceof MoveHandler moveHandler) {
        moveHandler.setDeleteExistingBeforeMove(false);
      }
    }
  }
}
