/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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

import org.italiangrid.storm.webdav.error.DiskQuotaExceeded;
import org.italiangrid.storm.webdav.error.ResourceNotFound;
import org.italiangrid.storm.webdav.error.SameFileError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.Filter;
import io.milton.http.FilterChain;
import io.milton.http.Handler;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.http.http11.Http11ResponseHandler;

public class StoRMMiltonBehaviour implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(StoRMMiltonBehaviour.class);

  @Override
  public void process(FilterChain chain, Request request, Response response) {

    final HttpManager manager = chain.getHttpManager();
    final Http11ResponseHandler responseHandler = manager.getResponseHandler();

    try {

      Request.Method method = request.getMethod();
      Handler handler = manager.getMethodHandler(method);

      if (handler == null) {
        responseHandler.respondMethodNotImplemented(new PhantomResource(request.getAbsolutePath()),
            response, request);
        return;
      }

      handler.process(manager, request, response);
      if (response.getEntity() != null) {
        manager.sendResponseEntity(response);
      }
    } catch (DiskQuotaExceeded e) {
      // responseHandler does not support sending insufficient storage
      response.sendError(Status.SC_INSUFFICIENT_STORAGE, e.getMessage());
    } catch (ResourceNotFound e) {
      responseHandler.respondNotFound(response, request);
    } catch (SameFileError e) {
      responseHandler.respondForbidden(null, response, request);
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
      responseHandler.respondServerError(request, response, t.getMessage());
    } finally {
      manager.closeResponse(response);
    }
  }
}
