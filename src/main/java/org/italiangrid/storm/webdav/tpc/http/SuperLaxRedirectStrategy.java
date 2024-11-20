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

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;

public class SuperLaxRedirectStrategy extends DefaultRedirectStrategy {

  public static final SuperLaxRedirectStrategy INSTANCE = new SuperLaxRedirectStrategy();

  private static final String[] REDIRECT_METHODS = new String[] {HttpGet.METHOD_NAME,
      HttpPost.METHOD_NAME, HttpPut.METHOD_NAME, HttpHead.METHOD_NAME, HttpDelete.METHOD_NAME};

  @Override
  public boolean isRedirected(final HttpRequest request, final HttpResponse response,
      final HttpContext context) {
    if (!response.containsHeader(HttpHeaders.LOCATION)) {
      return false;
    }

    final int statusCode = response.getCode();
    final String method = request.getMethod();
    final Header locationHeader = response.getFirstHeader("location");
    switch (statusCode) {
      case HttpStatus.SC_MOVED_TEMPORARILY:
        return isRedirectable(method) && locationHeader != null;
      case HttpStatus.SC_MOVED_PERMANENTLY, HttpStatus.SC_TEMPORARY_REDIRECT:
        return isRedirectable(method);
      case HttpStatus.SC_SEE_OTHER:
        return true;
      default:
        return false;
    }
  }

  protected boolean isRedirectable(final String method) {
    for (final String m : REDIRECT_METHODS) {
      if (m.equalsIgnoreCase(method)) {
        return true;
      }
    }
    return false;
  }
}

