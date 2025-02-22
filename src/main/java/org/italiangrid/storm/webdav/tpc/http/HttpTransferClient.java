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

import static java.lang.String.format;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.scitag.SciTagTransfer;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.TransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatusCallback;
import org.italiangrid.storm.webdav.tpc.transfer.error.TransferError;
import org.italiangrid.storm.webdav.tpc.utils.CountingFileEntity;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class HttpTransferClient implements TransferClient, DisposableBean {

  public static final Logger LOG = LoggerFactory.getLogger(HttpTransferClient.class);

  final Clock clock;
  final PathResolver resolver;
  final ExtendedAttributesHelper attributesHelper;
  final CloseableHttpClient httpClient;
  final ScheduledExecutorService executorService;
  final TransferStatus.Builder statusBuilder;
  final int reportDelaySec;
  final int localFileBufferSize;
  final int socketBufferSize;


  private void reportStatus(TransferStatusCallback cb, TransferRequest req, TransferStatus s) {
    req.setTransferStatus(s);
    cb.reportStatus(req, s);
  }

  public HttpTransferClient(Clock clock, CloseableHttpClient client, PathResolver pr,
      ExtendedAttributesHelper ah, @Qualifier("tpcProgressReportEs") ScheduledExecutorService es,
      ThirdPartyCopyProperties properties, ServiceConfigurationProperties config) {
    this.clock = clock;
    httpClient = client;
    resolver = pr;
    attributesHelper = ah;
    executorService = es;
    reportDelaySec = properties.getReportDelaySecs();
    localFileBufferSize = config.getBuffer().getFileBufferSizeBytes();
    socketBufferSize = properties.getHttpClientSocketBufferSize();
    statusBuilder = TransferStatus.builder(clock);
  }


  @Override
  public void destroy() throws Exception {
    httpClient.close();
    executorService.shutdownNow();
  }

  HttpGet prepareRequest(GetTransferRequest request) {
    request.setTransferStatus(TransferStatus.builder(clock).inProgress(0));

    HttpGet get = new HttpGet(request.remoteURI());

    for (Map.Entry<String, String> h : request.transferHeaders().entries()) {
      get.addHeader(h.getKey(), h.getValue());
    }
    return get;
  }

  HttpPut prepareRequest(PutTransferRequest request, HttpEntity cfe) {

    request.setTransferStatus(TransferStatus.builder(clock).inProgress(0));

    HttpPut put = new HttpPut(request.remoteURI());

    for (Map.Entry<String, String> h : request.transferHeaders().entries()) {
      put.addHeader(h.getKey(), h.getValue());
    }

    put.setEntity(cfe);

    return put;
  }

  CountingFileEntity prepareFileEntity(String path) {

    Objects.requireNonNull(path, "Impossible path resolution error");

    Path p = Paths.get(path);
    return CountingFileEntity.create(p.toFile());
  }

  StormCountingOutputStream prepareOutputStream(String path) {
    Objects.requireNonNull(path, "Impossible path resolution error");

    try {
      Path p = Paths.get(path);

      if (!p.toFile().exists()) {
        p = Files.createFile(p);
      }

      OutputStream fos = new FileOutputStream(new File(p.toString()));

      if (localFileBufferSize > 0) {
        fos = new BufferedOutputStream(fos, localFileBufferSize);
      }

      return StormCountingOutputStream.create(fos, p.toString());

    } catch (IOException e) {
      throw new TransferError(e.getMessage(), e);
    }
  }


  @Override
  public void handle(GetTransferRequest request, TransferStatusCallback cb) {

    StormCountingOutputStream os = prepareOutputStream(resolver.resolvePath(request.path()));
    HttpGet get = prepareRequest(request);
    HttpClientContext context = HttpClientContext.create();

    ScheduledFuture<?> reportTask = executorService.scheduleAtFixedRate(
        () -> reportStatus(cb, request, statusBuilder.inProgress(os.getCount())), reportDelaySec,
        reportDelaySec, TimeUnit.SECONDS);

    try {

      context.setAttribute(SciTag.SCITAG_ATTRIBUTE, request.scitag());
      httpClient.execute(get, new GetResponseHandler(request, os, attributesHelper,
          MDC.getCopyOfContextMap(), socketBufferSize, true), context);

      reportTask.cancel(true);
      reportStatus(cb, request, statusBuilder.done(os.getCount()));

    } catch (HttpResponseException e) {
      logException(e);
      reportStatus(cb, request, statusBuilder.error(format("Error fetching %s: %d %s",
          request.remoteURI().toString(), e.getStatusCode(), e.getMessage())));

    } catch (ClientProtocolException e) {
      logException(e);
      reportStatus(cb, request, statusBuilder
        .error(format("Error fetching %s: %s", request.remoteURI().toString(), e.getMessage())));

    } catch (Throwable e) {
      logException(e);
      reportStatus(cb, request, statusBuilder.error(format("%s while fetching %s: %s",
          e.getClass().getSimpleName(), request.remoteURI().toString(), e.getMessage())));
    } finally {
      if (!reportTask.isCancelled()) {
        reportTask.cancel(true);
      }
      SciTagTransfer scitagTransfer =
          (SciTagTransfer) context.getAttribute(SciTagTransfer.SCITAG_TRANSFER_ATTRIBUTE);
      if (scitagTransfer != null) {
        scitagTransfer.writeEnd();
      }
    }
  }

  protected void checkOverwrite(PutTransferRequest request) throws IOException {
    if (!request.overwrite()) {
      HttpHead head = new HttpHead(request.remoteURI());
      for (Map.Entry<String, String> h : request.transferHeaders().entries()) {
        head.addHeader(h.getKey(), h.getValue());
      }
      CloseableHttpResponse response = httpClient.execute(head);
      if (response.getStatusLine().getStatusCode() == 200) {
        throw new TransferError("Remote file exists and overwrite is false");
      } else if (response.getStatusLine().getStatusCode() != 404) {
        throw new TransferError(format("Error checking if remote file exists: %d %s",
            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
      }
    }
  }

  @Override
  public void handle(PutTransferRequest request, TransferStatusCallback cb) {

    CountingFileEntity cfe = prepareFileEntity(resolver.resolvePath(request.path()));

    HttpPut put = null;
    HttpClientContext context = HttpClientContext.create();

    put = prepareRequest(request, cfe);

    ScheduledFuture<?> reportTask = executorService.scheduleAtFixedRate(
        () -> reportStatus(cb, request, statusBuilder.inProgress(cfe.getCount())), reportDelaySec,
        reportDelaySec, TimeUnit.SECONDS);

    try {
      checkOverwrite(request);
      context.setAttribute(SciTag.SCITAG_ATTRIBUTE, request.scitag());
      httpClient.execute(put, new PutResponseHandler(MDC.getCopyOfContextMap()), context);
      reportTask.cancel(true);
      reportStatus(cb, request, statusBuilder.done(cfe.getCount()));
    } catch (HttpResponseException e) {
      logException(e);
      reportStatus(cb, request, statusBuilder.error(format("Error pushing %s: %d %s",
          request.remoteURI().toString(), e.getStatusCode(), e.getMessage())));
    } catch (ClientProtocolException e) {
      logException(e);
      reportStatus(cb, request, statusBuilder
        .error(format("Error pushing %s: %s", request.remoteURI().toString(), e.getMessage())));
    } catch (Throwable e) {
      LOG.error(e.getMessage(), e); // we explicitly always log a generic error
      reportStatus(cb, request, statusBuilder.error(format("%s while pushing %s: %s",
          e.getClass().getSimpleName(), request.remoteURI().toString(), e.getMessage())));
    } finally {
      if (!reportTask.isCancelled()) {
        reportTask.cancel(true);
      }
      SciTagTransfer scitagTransfer =
          (SciTagTransfer) context.getAttribute(SciTagTransfer.SCITAG_TRANSFER_ATTRIBUTE);
      if (scitagTransfer != null) {
        scitagTransfer.writeEnd();
      }
    }
  }

  private void logException(Throwable e) {
    if (LOG.isDebugEnabled()) {
      LOG.error(e.getMessage(), e);
    }
  }
}
