// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.redirector;

import java.net.URI;

import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.RedirectorProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.RedirectorProperties.ReplicaEndpointProperties;
import org.italiangrid.storm.webdav.test.utils.TestUtils;

public class RedirectorTestSupport implements TestUtils {


  public static final String PATH = "/example/file";
  public static final String PATH_WITH_PREFIX = "/prefix" + PATH;

  public static final String RANDOM_TOKEN_STRING = "dshakd0123";
  public static final String ACCESS_TOKEN_QUERY_STRING = "access_token=" + RANDOM_TOKEN_STRING;

  public static final String URI_0_SCHEME = "http";
  public static final String URI_1_SCHEME = "https";
  public static final String URI_WITH_PREFIX_SCHEME = "http";

  public static final String URI_0_HOST = "example";
  public static final String URI_1_HOST = "another";
  public static final String URI_WITH_PREFIX_HOST = "yetanother";

  public static final URI ENDPOINT_URI_0 =
      URI.create(String.format("%s://%s", URI_0_SCHEME, URI_0_HOST));
  public static final URI ENDPOINT_URI_1 =
      URI.create(String.format("%s://%s", URI_1_SCHEME, URI_1_HOST));

  public static final URI ENDPOINT_URI_WITH_PREFIX =
      URI.create(String.format("%s://%s/prefix", URI_WITH_PREFIX_SCHEME, URI_WITH_PREFIX_HOST));

  public static final ReplicaEndpointProperties REPLICA_0;
  public static final ReplicaEndpointProperties REPLICA_1;
  public static final ReplicaEndpointProperties REPLICA_WITH_PREFIX;

  static {
    REPLICA_0 = new ReplicaEndpointProperties();
    REPLICA_0.setEndpoint(ENDPOINT_URI_0);

    REPLICA_1 = new ReplicaEndpointProperties();
    REPLICA_1.setEndpoint(ENDPOINT_URI_1);

    REPLICA_WITH_PREFIX = new ReplicaEndpointProperties();
    REPLICA_WITH_PREFIX.setEndpoint(ENDPOINT_URI_WITH_PREFIX);
  }

  protected ServiceConfigurationProperties buildConfigurationProperties() {
    ServiceConfigurationProperties config = new ServiceConfigurationProperties();

    RedirectorProperties properties = new RedirectorProperties();
    config.setRedirector(properties);

    return config;
  }

}
