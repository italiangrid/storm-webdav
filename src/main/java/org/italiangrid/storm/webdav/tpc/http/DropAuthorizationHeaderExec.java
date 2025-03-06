// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.http;

import java.io.IOException;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;

public final class DropAuthorizationHeaderExec implements ExecChainHandler {

  private final RedirectStrategy redirectStrategy;

  public DropAuthorizationHeaderExec(final RedirectStrategy redirectStrategy) {
    super();
    this.redirectStrategy = redirectStrategy;
  }

  @Override
  public ClassicHttpResponse execute(
      final ClassicHttpRequest request, final ExecChain.Scope scope, final ExecChain chain)
      throws IOException, HttpException {
    final ClassicHttpResponse response = chain.proceed(request, scope);
    final HttpClientContext context = scope.clientContext;
    final RequestConfig config = context.getRequestConfigOrDefault();
    if (config.isRedirectsEnabled()
        && this.redirectStrategy.isRedirected(request, response, context)) {
      scope.originalRequest.removeHeaders(HttpHeaders.AUTHORIZATION);
    }
    return response;
  }
}
