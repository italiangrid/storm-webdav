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
  public ClassicHttpResponse execute(final ClassicHttpRequest request, final ExecChain.Scope scope,
      final ExecChain chain) throws IOException, HttpException {
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
