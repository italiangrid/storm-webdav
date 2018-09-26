package org.italiangrid.storm.webdav.test.tpc.http;

import java.net.URI;
import java.nio.file.FileSystem;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.http.HttpTransferClient;
import org.italiangrid.storm.webdav.tpc.transfer.GetTransferRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.PathType;

public class ClientTestSupport {

  public static final String SA_ROOT = "/test";
  public static final String LOCAL_PATH = "/test/file";
  public static final String HTTP_URI = "http://storm.example/test/file";
  public static final URI HTTP_URI_URI = URI.create(HTTP_URI);

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String AUTHORIZATION_HEADER_VALUE = "Bearer 12345";
  
  public static final Multimap<String, String> HEADER_MAP =
      new ImmutableMultimap.Builder<String, String>()
        .put(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE)
        .build();

  @Mock
  PathResolver resolver;

  @Mock
  ExtendedAttributesHelper eah;

  @Mock
  CloseableHttpClient httpClient;

  @Mock
  GetTransferRequest req;

  @InjectMocks
  HttpTransferClient client;

  @Captor
  ArgumentCaptor<HttpGet> getRequest;

  public static final String MOCKFS_WORKDIR = "/mockfs";

  public FileSystem initMockFs() {
    Configuration fsConfig = Configuration.builder(PathType.unix())
      .setRoots("/")
      .setWorkingDirectory(MOCKFS_WORKDIR)
      .setAttributeViews("basic", "owner", "posix", "unix", "user")
      .build();

    return Jimfs.newFileSystem(fsConfig);
  }
}
