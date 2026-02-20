// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.AuthenticationHandler;
import io.milton.http.http11.DefaultHttp11ResponseHandler.BUFFERING;
import java.util.List;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.PathResolver;

public class StoRMHTTPManagerBuilder extends HttpManagerBuilder {

  public StoRMHTTPManagerBuilder(
      ExtendedAttributesHelper attrsHelper,
      PathResolver resolver,
      boolean deleteFilesWithMismatchedChecksums) {

    setDefaultStandardFilter(
        new StoRMMiltonBehaviour(attrsHelper, resolver, deleteFilesWithMismatchedChecksums));
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
}
