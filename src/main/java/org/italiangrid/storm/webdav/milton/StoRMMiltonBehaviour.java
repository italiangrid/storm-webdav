package org.italiangrid.storm.webdav.milton;

import io.milton.http.Filter;
import io.milton.http.FilterChain;
import io.milton.http.Handler;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.http.http11.Http11ResponseHandler;

import org.italiangrid.storm.webdav.error.ResourceNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoRMMiltonBehaviour implements Filter {

  private static final Logger LOG = LoggerFactory
    .getLogger(StoRMMiltonBehaviour.class);

  @Override
  public void process(FilterChain chain, Request request, Response response) {

    final HttpManager manager = chain.getHttpManager();
    final Http11ResponseHandler responseHandler = manager.getResponseHandler();

    try {

      Request.Method method = request.getMethod();
      Handler handler = manager.getMethodHandler(method);

      if (handler == null) {
        responseHandler.respondMethodNotImplemented(
          new PhantomResource(request.getAbsolutePath()), response, request);
        return;
      }

      handler.process(manager, request, response);
      if (response.getEntity() != null) {
        manager.sendResponseEntity(response);
      }
    } catch (ResourceNotFound e) {
      responseHandler.respondNotFound(response, request);
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
      responseHandler.respondServerError(request, response, t.getMessage());
    } finally {
      manager.closeResponse(response);
    }

  }

}
