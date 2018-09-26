package org.italiangrid.storm.webdav.tpc.http;

import static java.lang.String.format;
import static org.italiangrid.storm.webdav.tpc.utils.Adler32DigestHeaderHelper.extractAdler32DigestFromResponse;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumOutputStream;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.tpc.transfer.TransferStatusCallback;
import org.italiangrid.storm.webdav.tpc.transfer.error.ChecksumVerificationError;
import org.italiangrid.storm.webdav.tpc.utils.StormCountingOutputStream;

public class GetResponseHandler implements org.apache.http.client.ResponseHandler<Boolean> {

  final TransferStatusCallback statusCallback;
  final StormCountingOutputStream fileStream;
  final ExtendedAttributesHelper attributesHelper;

  final boolean verifyChecksum;

  public GetResponseHandler(boolean verifyChecksum, StormCountingOutputStream fs,
      TransferStatusCallback cb, ExtendedAttributesHelper ah) {
    this.verifyChecksum = verifyChecksum;
    statusCallback = cb;
    fileStream = fs;
    attributesHelper = ah;
  }

  private void checkResponseStatus(StatusLine sl) throws HttpResponseException {
    if (sl.getStatusCode() >= 300) {
      throw new HttpResponseException(sl.getStatusCode(), sl.getReasonPhrase());
    }
  }

  private void verifyChecksum(HttpResponse response, Adler32ChecksumOutputStream checkedStream) {
    Optional<String> checksum = extractAdler32DigestFromResponse(response);

    if (!checksum.isPresent()) {

      throw new ChecksumVerificationError("Digest header not found in response");

    } else if (!checkedStream.getChecksumValue().equals(checksum.get())) {

      throw new ChecksumVerificationError(
          format("Adler32 checksum verification error: expected=%s actual=%s", checksum.get(),
              checkedStream.getChecksumValue()));
    }
  }

  @Override
  public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();

    checkResponseStatus(sl);

    Adler32ChecksumOutputStream checkedStream = new Adler32ChecksumOutputStream(fileStream);

    try {
      if (entity != null) {

        entity.writeTo(checkedStream);

        if (verifyChecksum) {
          verifyChecksum(response, checkedStream);
        }

        attributesHelper.setChecksumAttribute(fileStream.getPath(),
            checkedStream.getChecksumValue());
      }
      return true;

    } finally {
      fileStream.close();
    }

  }

}
