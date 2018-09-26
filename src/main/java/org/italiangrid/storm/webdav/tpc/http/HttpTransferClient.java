package org.italiangrid.storm.webdav.tpc.http;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.PutTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatusCallback;
import org.italiangrid.storm.webdav.tpc.transfer.error.TransferError;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HttpTransferClient implements TransferClient, DisposableBean {

  final PathResolver resolver;
  final ExtendedAttributesHelper attributesHelper;

  final CloseableHttpClient httpClient;

  @Autowired
  public HttpTransferClient(CloseableHttpClient client, PathResolver pr,
      ExtendedAttributesHelper ah) {
    httpClient = client;
    resolver = pr;
    attributesHelper = ah;
  }


  @Override
  public void destroy() throws Exception {
    httpClient.close();
  }

  HttpGet prepareRequest(GetTransferRequest request) {
    HttpGet get = new HttpGet(request.remoteURI());

    for (Map.Entry<String, String> h : request.transferHeaders().entries()) {
      get.addHeader(h.getKey(), h.getValue());
    }
    return get;
  }

  StormCountingOutputStream prepareOutputStream(String path) {
    checkNotNull(path, "Impossible path resolution error");

    try {
      Path p = Paths.get(path);

      if (!Files.exists(p)) {
        p = Files.createFile(p);
      }

      FileOutputStream fos = new FileOutputStream(new File(p.toString()));
      return StormCountingOutputStream.create(fos, p.toString());

    } catch (IOException e) {
      throw new TransferError(e.getMessage(), e);
    }
  }

  @Override
  public void handle(GetTransferRequest request, TransferStatusCallback status) {

    StormCountingOutputStream os = prepareOutputStream(resolver.resolvePath(request.path()));
    HttpGet get = prepareRequest(request);

    try {
      httpClient.execute(get,
          new GetResponseHandler(request.verifyChecksum(), os, status, attributesHelper));
    } catch (IOException e) {
      throw new TransferError(e.getMessage(), e);
    }
  }

  @Override
  public void handle(PutTransferRequest request, TransferStatusCallback status) {

  }
}
