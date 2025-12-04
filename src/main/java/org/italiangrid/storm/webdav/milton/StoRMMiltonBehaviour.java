// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton;

import io.milton.http.Filter;
import io.milton.http.FilterChain;
import io.milton.http.Handler;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.http11.Http11ResponseHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.italiangrid.storm.webdav.error.DirectoryNotEmpty;
import org.italiangrid.storm.webdav.error.InsufficientStorage;
import org.italiangrid.storm.webdav.error.ResourceNotFound;
import org.italiangrid.storm.webdav.error.SameFileError;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.TransferConstants;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.italiangrid.storm.webdav.tpc.utils.Adler32DigestHeaderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.MethodNotAllowedException;

public class StoRMMiltonBehaviour implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(StoRMMiltonBehaviour.class);

  private final ExtendedAttributesHelper attrsHelper;

  private final PathResolver resolver;

  private final boolean deleteFilesWithMismatchedChecksums;

  public StoRMMiltonBehaviour(
      ExtendedAttributesHelper attrsHelper,
      PathResolver resolver,
      boolean deleteFilesWithMismatchedChecksums) {
    this.attrsHelper = attrsHelper;
    this.resolver = resolver;
    this.deleteFilesWithMismatchedChecksums = deleteFilesWithMismatchedChecksums;
  }

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
      if (request.getMethod() == Method.PUT) {
        putRequestHandling(request);
      }
      if (response.getEntity() != null) {
        manager.sendResponseEntity(response);
      }
    } catch (InsufficientStorage e) {
      // responseHandler does not support sending insufficient storage
      response.sendError(Status.SC_INSUFFICIENT_STORAGE, e.getMessage());
    } catch (ResourceNotFound e) {
      responseHandler.respondNotFound(response, request);
    } catch (SameFileError e) {
      responseHandler.respondForbidden(null, response, request);
    } catch (MethodNotAllowedException e) {
      response.setAllowHeader(e.getSupportedMethods().stream().map(Object::toString).toList());
      responseHandler.respondMethodNotAllowed(
          new PhantomResource(request.getAbsolutePath()), response, request);
    } catch (ConflictException e) {
      responseHandler.respondConflict(e.getResource(), response, request, e.getMessage());
    } catch (BadRequestException e) {
      responseHandler.respondBadRequest(e.getResource(), response, request);
    } catch (DirectoryNotEmpty | ChecksumVerificationError e) {
      sendError(response, Status.SC_PRECONDITION_FAILED, e.getMessage());
    } catch (NotAuthorizedException e) {
      // Message is vague to avoid leaking information on why the request was forbidden.
      sendError(response, Status.SC_FORBIDDEN, "Permission denied.");
    } catch (Exception t) {
      LOG.error(t.getMessage(), t);
      responseHandler.respondServerError(request, response, t.getMessage());
    } finally {
      manager.closeResponse(response);
    }
  }

  public void sendError(Response r, Status statusCode, String message) {
    r.setStatus(statusCode);
    r.setContentTypeHeader("text/plain");
    try {
      r.getOutputStream().write(message.getBytes());
      r.getOutputStream().close();
    } catch (IOException e) {

    }
  }

  public void putRequestHandling(Request request) throws ChecksumVerificationError {
    // The PASSIVE site is expected to verify that the provided checksum matches the one of the
    // new saved file.
    // Milton headers are lowercase
    Adler32DigestHeaderHelper.extractAdler32DigestFromHeaderValue(
            request.getHeaders().get(TransferConstants.REPR_DIGEST_HEADER.toLowerCase()))
        .ifPresent(
            reprDigestChecksum -> {
              Path filePath = resolver.getPath(request.getAbsolutePath());
              String checksum;
              try {
                checksum = attrsHelper.getChecksumAttribute(filePath);
              } catch (IOException e) {
                throw new ChecksumVerificationError(
                    "Error retrieving checksum from the file system");
              }
              if (!checksum.equals(reprDigestChecksum)) {
                if (deleteFilesWithMismatchedChecksums) {
                  try {
                    Files.delete(filePath);
                  } catch (IOException e) {
                    LOG.warn(
                        "Cannot delete file received with the wrong checksum {}: {}",
                        resolver.resolvePath(request.getAbsolutePath()),
                        e.getMessage(),
                        e);
                  }
                }
                throw new ChecksumVerificationError("client/server checksum mismatch");
              }
            });
  }
}
