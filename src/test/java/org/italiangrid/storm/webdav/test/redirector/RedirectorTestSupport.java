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
