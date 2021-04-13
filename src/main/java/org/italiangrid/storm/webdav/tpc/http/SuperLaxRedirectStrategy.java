/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class SuperLaxRedirectStrategy extends DefaultRedirectStrategy {

  /*
   * SUH stands for Sad, Useless Header.
   */
  public static final String SUH_HEADER = "X-SUH";
  
  public static final String AUTHORIZATION_HEADER = "Authorization";
  
  private static final String[] REDIRECT_METHODS = new String[] {HttpGet.METHOD_NAME,
      HttpPut.METHOD_NAME, HttpPost.METHOD_NAME, HttpHead.METHOD_NAME, HttpDelete.METHOD_NAME};

  public static final SuperLaxRedirectStrategy INSTANCE = new SuperLaxRedirectStrategy();

  private SuperLaxRedirectStrategy() {
    // empty ctor
  }

  @Override
  public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
      throws ProtocolException {

    HttpUriRequest redirect = super.getRedirect(request, response, context);

    /*
     * If this method returns an HttpUriRequest that has no HTTP headers then the RedirectExec code
     * will copy all the headers from the original request into the HttpUriRequest.
     * DefaultRedirectStrategy returns such requests under several circumstances. Therefore, in
     * order to suppress the Authorization header we <em>must</em> ensure the returned request
     * includes headers.
     */
    if (!redirect.headerIterator().hasNext()) {
      redirect.setHeaders(request.getAllHeaders());
    }

    redirect.removeHeaders(AUTHORIZATION_HEADER);
    
    if (!redirect.headerIterator().hasNext()) {
      /*
       * If the Authorization header was the only one set in the original request or in the redirect, we need to
       * add back an empty header otherwise the RedirectExec code  will copy the Authorization header from the original
       * request back in.
       */
      redirect.addHeader(SUH_HEADER, "");
    }
    
    return redirect;
  }


  @Override
  protected boolean isRedirectable(String method) {
    for (final String m : REDIRECT_METHODS) {
      if (m.equalsIgnoreCase(method)) {
        return true;
      }
    }

    return false;
  }
}
