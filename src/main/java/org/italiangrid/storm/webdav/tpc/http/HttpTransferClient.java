// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.http;

import static java.lang.String.format;

import io.micrometer.core.instrument.binder.httpcomponents.hc5.ApacheHttpClientContext;
import io.micrometer.core.instrument.binder.httpcomponents.hc5.ApacheHttpClientObservationDocumentation;
import io.micrometer.core.instrument.binder.httpcomponents.hc5.DefaultApacheHttpClientObservationConvention;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
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
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.scitag.SciTag;
import org.italiangrid.storm.webdav.scitag.SciTagTransfer;
import org.italiangrid.storm.webdav.server.PathResolver;
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
  final ObservationRegistry observationRegistry;
  final HttpComponentsMetrics httpComponentsMetrics;
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

  public HttpTransferClient(
      Clock clock,
      CloseableHttpClient client,
      PathResolver pr,
      ExtendedAttributesHelper ah,
      @Qualifier("tpcProgressReportEs") ScheduledExecutorService es,
      ThirdPartyCopyProperties properties,
      ServiceConfigurationProperties config,
      ObservationRegistry observationRegistry,
      HttpComponentsMetrics httpComponentsMetrics) {
    this.clock = clock;
    httpClient = client;
    this.observationRegistry = observationRegistry;
    this.httpComponentsMetrics = httpComponentsMetrics;
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

  BasicClassicHttpRequest prepareRequest(GetTransferRequest request) {
    request.setTransferStatus(TransferStatus.builder(clock).inProgress(0));

    BasicClassicHttpRequest get = new BasicClassicHttpRequest(Method.GET, request.remoteURI());

    for (Map.Entry<String, String> h : request.transferHeaders().entries()) {
      get.addHeader(h.getKey(), h.getValue());
    }
    return get;
  }

  BasicClassicHttpRequest prepareRequest(PutTransferRequest request, HttpEntity cfe) {

    request.setTransferStatus(TransferStatus.builder(clock).inProgress(0));

    BasicClassicHttpRequest put = new BasicClassicHttpRequest(Method.PUT, request.remoteURI());

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
    BasicClassicHttpRequest get = prepareRequest(request);
    HttpClientContext context = HttpClientContext.create();
    Observation observation = null;
    BytesCount bytesCount = new BytesCount();

    ScheduledFuture<?> reportTask =
        executorService.scheduleAtFixedRate(
            () -> {
              reportStatus(cb, request, statusBuilder.inProgress(os.getCount()));
              bytesCount.updateMetrics(context);
            },
            reportDelaySec,
            reportDelaySec,
            TimeUnit.SECONDS);
    try {
      context.setAttribute(SciTag.SCITAG_ATTRIBUTE, request.scitag());
      ApacheHttpClientContext observationContext = new ApacheHttpClientContext(get, context);
      observation =
          ApacheHttpClientObservationDocumentation.DEFAULT.observation(
              null,
              DefaultApacheHttpClientObservationConvention.INSTANCE,
              () -> observationContext,
              observationRegistry);
      observation.start();

      httpClient.execute(
          get,
          context,
          new GetResponseHandler(
              request,
              os,
              attributesHelper,
              MDC.getCopyOfContextMap(),
              socketBufferSize,
              true,
              observationContext));
      reportTask.cancel(true);
      reportStatus(cb, request, statusBuilder.done(os.getCount()));
    } catch (HttpResponseException e) {
      logException(e);
      reportStatus(
          cb,
          request,
          statusBuilder.error(
              format(
                  "Error fetching %s: %d %s",
                  request.remoteURI().toString(), e.getStatusCode(), e.getMessage())));
      observation.error(e);

    } catch (ClientProtocolException e) {
      logException(e);
      reportStatus(
          cb,
          request,
          statusBuilder.error(
              format("Error fetching %s: %s", request.remoteURI().toString(), e.getMessage())));
      observation.error(e);

    } catch (Throwable e) {
      logException(e);
      reportStatus(
          cb,
          request,
          statusBuilder.error(
              format(
                  "%s while fetching %s: %s",
                  e.getClass().getSimpleName(), request.remoteURI().toString(), e.getMessage())));
      observation.error(e);

    } finally {
      if (!reportTask.isCancelled()) {
        reportTask.cancel(true);
      }
      SciTagTransfer scitagTransfer =
          (SciTagTransfer) context.getAttribute(SciTagTransfer.SCITAG_TRANSFER_ATTRIBUTE);
      if (scitagTransfer != null) {
        scitagTransfer.writeEnd();
      }
      bytesCount.updateMetrics(context);
      if (observation != null) {
        observation.stop();
      }
    }
  }

  protected void checkOverwrite(PutTransferRequest request) throws IOException {
    if (!request.overwrite()) {
      HttpHead head = new HttpHead(request.remoteURI());
      for (Map.Entry<String, String> h : request.transferHeaders().entries()) {
        head.addHeader(h.getKey(), h.getValue());
      }
      httpClient.execute(
          head,
          response -> {
            if (response.getCode() == 200) {
              throw new TransferError("Remote file exists and overwrite is false");
            } else if (response.getCode() != 404) {
              throw new TransferError(
                  format(
                      "Error checking if remote file exists: %d %s",
                      response.getCode(), response.getReasonPhrase()));
            }
            return null;
          });
    }
  }

  @Override
  public void handle(PutTransferRequest request, TransferStatusCallback cb) {

    CountingFileEntity cfe = prepareFileEntity(resolver.resolvePath(request.path()));

    BasicClassicHttpRequest put = null;
    HttpClientContext context = HttpClientContext.create();
    Observation observation = null;
    BytesCount bytesCount = new BytesCount();

    put = prepareRequest(request, cfe);

    ScheduledFuture<?> reportTask =
        executorService.scheduleAtFixedRate(
            () -> {
              reportStatus(cb, request, statusBuilder.inProgress(cfe.getCount()));
              bytesCount.updateMetrics(context);
            },
            reportDelaySec,
            reportDelaySec,
            TimeUnit.SECONDS);

    try {
      checkOverwrite(request);
      context.setAttribute(SciTag.SCITAG_ATTRIBUTE, request.scitag());
      ApacheHttpClientContext observationContext = new ApacheHttpClientContext(put, context);
      observation =
          ApacheHttpClientObservationDocumentation.DEFAULT.observation(
              null,
              DefaultApacheHttpClientObservationConvention.INSTANCE,
              () -> observationContext,
              observationRegistry);
      observation.start();

      httpClient.execute(
          put, context, new PutResponseHandler(MDC.getCopyOfContextMap(), observationContext));
      reportTask.cancel(true);
      reportStatus(cb, request, statusBuilder.done(cfe.getCount()));
    } catch (HttpResponseException e) {
      logException(e);
      reportStatus(
          cb,
          request,
          statusBuilder.error(
              format(
                  "Error pushing %s: %d %s",
                  request.remoteURI().toString(), e.getStatusCode(), e.getMessage())));
      observation.error(e);
    } catch (ClientProtocolException e) {
      logException(e);
      reportStatus(
          cb,
          request,
          statusBuilder.error(
              format("Error pushing %s: %s", request.remoteURI().toString(), e.getMessage())));
      observation.error(e);
    } catch (Throwable e) {
      LOG.error(e.getMessage(), e); // we explicitly always log a generic error
      reportStatus(
          cb,
          request,
          statusBuilder.error(
              format(
                  "%s while pushing %s: %s",
                  e.getClass().getSimpleName(), request.remoteURI().toString(), e.getMessage())));
      observation.error(e);
    } finally {
      if (!reportTask.isCancelled()) {
        reportTask.cancel(true);
      }
      SciTagTransfer scitagTransfer =
          (SciTagTransfer) context.getAttribute(SciTagTransfer.SCITAG_TRANSFER_ATTRIBUTE);
      if (scitagTransfer != null) {
        scitagTransfer.writeEnd();
      }
      bytesCount.updateMetrics(context);
      if (observation != null) {
        observation.stop();
      }
    }
  }

  private void logException(Throwable e) {
    if (LOG.isDebugEnabled()) {
      LOG.error(e.getMessage(), e);
    }
  }

  private class BytesCount {
    double received = 0;
    double sent = 0;

    public void updateMetrics(HttpClientContext context) {
      EndpointDetails metrics = context.getEndpointDetails();
      if (metrics != null) {
        double receivedBytesCount = metrics.getReceivedBytesCount();
        double sentBytesCount = metrics.getSentBytesCount();
        httpComponentsMetrics.incoming(receivedBytesCount - this.received);
        httpComponentsMetrics.outgoing(sentBytesCount - this.sent);
        this.received = receivedBytesCount;
        this.sent = sentBytesCount;
      }
    }
  }
}
