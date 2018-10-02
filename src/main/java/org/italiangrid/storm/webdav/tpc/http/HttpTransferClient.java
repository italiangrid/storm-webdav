/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatus;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatusCallback;
import org.italiangrid.storm.webdav.tpc.transfer.error.TransferError;
import org.italiangrid.storm.webdav.tpc.utils.CountingFileEntity;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpTransferClient implements TransferClient, DisposableBean {

  final PathResolver resolver;
  final ExtendedAttributesHelper attributesHelper;
  final CloseableHttpClient httpClient;
  final ScheduledExecutorService executorService;
  final int reportDelaySec;
  final int localFileBufferSize;

  @Autowired
  public HttpTransferClient(CloseableHttpClient client, PathResolver pr,
      ExtendedAttributesHelper ah, ScheduledExecutorService es,
      @Value("${tpc.reportDelaySecs}") int reportDelaySeconds,
      @Value("${tpc.localFileBufferSize}") int lfbs) {
    httpClient = client;
    resolver = pr;
    attributesHelper = ah;
    executorService = es;
    reportDelaySec = reportDelaySeconds;
    localFileBufferSize = lfbs;
  }


  @Override
  public void destroy() throws Exception {
    httpClient.close();
    executorService.shutdownNow();
  }

  HttpGet prepareRequest(GetTransferRequest request) {
    HttpGet get = new HttpGet(request.remoteURI());

    for (Map.Entry<String, String> h : request.transferHeaders().entries()) {
      get.addHeader(h.getKey(), h.getValue());
    }
    return get;
  }

  HttpPut prepareRequest(PutTransferRequest request, HttpEntity cfe) {

    HttpPut put = new HttpPut(request.remoteURI());

    for (Map.Entry<String, String> h : request.transferHeaders().entries()) {
      put.addHeader(h.getKey(), h.getValue());
    }

    put.setEntity(cfe);

    return put;
  }

  CountingFileEntity prepareFileEntity(String path) {
    
    checkNotNull(path, "Impossible path resolution error");

    Path p = Paths.get(path);

    try {
      return CountingFileEntity.create(p.toFile());
    } catch (FileNotFoundException e) {
      throw new TransferError("Resolved path does not exists!", e);
    }
  }

  StormCountingOutputStream prepareOutputStream(String path) {
    checkNotNull(path, "Impossible path resolution error");

    try {
      Path p = Paths.get(path);

      if (!Files.exists(p)) {
        p = Files.createFile(p);
      }

      BufferedOutputStream fos = new BufferedOutputStream(
          new FileOutputStream(new File(p.toString())), localFileBufferSize);

      return StormCountingOutputStream.create(fos, p.toString());

    } catch (IOException e) {
      throw new TransferError(e.getMessage(), e);
    }
  }

  @Override
  public void handle(GetTransferRequest request, TransferStatusCallback status) {

    StormCountingOutputStream os = prepareOutputStream(resolver.resolvePath(request.path()));
    HttpGet get = prepareRequest(request);

    ScheduledFuture<?> reportTask = executorService.scheduleAtFixedRate(() -> {
      status.reportStatus(TransferStatus.inProgress(os.getCount()));
    }, reportDelaySec, reportDelaySec, TimeUnit.SECONDS);

    try {

      httpClient.execute(get, new GetResponseHandler(os, attributesHelper));

      reportTask.cancel(true);
      status.reportStatus(TransferStatus.done(os.getCount()));

    } catch (HttpResponseException e) {
      status.reportStatus(TransferStatus.error(format("Error fetching %s: %d %s",
          request.remoteURI().toString(), e.getStatusCode(), e.getMessage())));
    } catch (ClientProtocolException e) {
      status.reportStatus(TransferStatus
        .error(format("Error fetching %s: %s", request.remoteURI().toString(), e.getMessage())));
    } catch (Throwable e) {
      status.reportStatus(TransferStatus.error(format("%s while fetching %s: %s",
          e.getClass().getSimpleName(), request.remoteURI().toString(), e.getMessage())));
    } finally {
      if (!reportTask.isCancelled()) {
        reportTask.cancel(true);
      }
    }
  }

  protected void checkOverwrite(PutTransferRequest request)
      throws ClientProtocolException, IOException {
    if (!request.overwrite()) {
      HttpHead head = new HttpHead(request.remoteURI());
      for (Map.Entry<String, String> h : request.transferHeaders().entries()) {
        head.addHeader(h.getKey(), h.getValue());
      }
      CloseableHttpResponse response = httpClient.execute(head);
      if (response.getStatusLine().getStatusCode() == 200) {
        throw new TransferError(
            "Remote file exists and overwrite is false");
      } else if (response.getStatusLine().getStatusCode() != 404) {
        throw new TransferError(format("Error checking if remote file exists: %d %s",
            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
      }
    }
  }

  @Override
  public void handle(PutTransferRequest request, TransferStatusCallback status) {
    
    CountingFileEntity cfe = prepareFileEntity(resolver.resolvePath(request.path()));
    
    HttpPut put = prepareRequest(request, cfe);

    ScheduledFuture<?> reportTask = executorService.scheduleAtFixedRate(() -> {
      status.reportStatus(TransferStatus.inProgress(cfe.getCount()));
    }, reportDelaySec, reportDelaySec, TimeUnit.SECONDS);

    try {
      checkOverwrite(request);
      httpClient.execute(put, new PutResponseHandler());
      reportTask.cancel(true);
      status.reportStatus(TransferStatus.done(10));
    } catch (HttpResponseException e) {
      status.reportStatus(TransferStatus.error(format("Error pushing %s: %d %s",
          request.remoteURI().toString(), e.getStatusCode(), e.getMessage())));
    } catch (ClientProtocolException e) {
      status.reportStatus(TransferStatus
        .error(format("Error pushing %s: %s", request.remoteURI().toString(), e.getMessage())));
    } catch (Throwable e) {
      status.reportStatus(TransferStatus.error(format("%s while pushing %s: %s",
          e.getClass().getSimpleName(), request.remoteURI().toString(), e.getMessage())));
    } finally {
      if (!reportTask.isCancelled()) {
        reportTask.cancel(true);
      }
    }
  }
}
