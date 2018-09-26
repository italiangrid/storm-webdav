package org.italiangrid.storm.webdav.tpc;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequestBuilder;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.italiangrid.storm.webdav.tpc.transfer.error.TransferError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferFilter extends TransferFilterSupport implements Filter {

  public static final Logger LOG = LoggerFactory.getLogger(TransferFilter.class);

  final TransferClient client;

  public TransferFilter(TransferClient c, PathResolver resolver, boolean verifyChecksum) {
    super(resolver, verifyChecksum);
    client = c;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    if (isTpc(req)) {
      handleCopy(req, res);
    } else {
      chain.doFilter(request, response);
    }
  }

  protected void handleCopy(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    if (validRequest(request, response)) {
      Optional<String> source = Optional.ofNullable(request.getHeader(SOURCE_HEADER));
      if (source.isPresent()) {
        handlePullCopy(request, response);
      } else {
        handlePushCopy(request, response);
      }
    }
  }

  @Override
  public void destroy() {

  }

  protected void handlePullCopy(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    URI uri = URI.create(request.getHeader(SOURCE_HEADER));
    String path = getScopedPathInfo(request);

    GetTransferRequest xferRequest = GetTransferRequestBuilder.create()
      .uri(uri)
      .path(path)
      .headers(getTransferHeaders(request, response))
      .verifyChecksum(verifyChecksum && verifyChecksumRequested(request))
      .overwrite(overwriteRequested(request))
      .build();

    try {
      
      client.handle(xferRequest, s -> {
      });
      
      response.setStatus(SC_CREATED);
      
    } catch (ChecksumVerificationError e) {
      handleChecksumVerificationError(e, response);
    } catch(TransferError e) {
      handleTransferError(e, response);
    } catch (HttpResponseException e) {
      handleHttpResponseException(e, response);
    } catch(ClientProtocolException e) {
      handleClientProtocolException(e, response);
    } 
  }

  protected void handlePushCopy(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // TBD
  }

}
