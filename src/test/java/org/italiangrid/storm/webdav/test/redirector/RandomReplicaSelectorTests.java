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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.RedirectorProperties.ReplicaEndpointProperties;
import org.italiangrid.storm.webdav.redirector.RandomReplicaSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
class RandomReplicaSelectorTests extends RedirectorTestSupport {

  ServiceConfigurationProperties config;
  RandomReplicaSelector selector;

  @BeforeEach
  void setup() {
    config = buildConfigurationProperties();
    selector = new RandomReplicaSelector(config);
  }

  @Test
  void testEmptyOptionalOnEmptyEndpointList() {

    assertThat(selector.selectReplica().isPresent(), is(false));

  }

  @Test
  void testSingleEndpointList() {

    ReplicaEndpointProperties replica = new ReplicaEndpointProperties();
    replica.setEndpoint(ENDPOINT_URI_0);

    config.getRedirector().getPool().getEndpoints().add(replica);

    assertThat(selector.selectReplica().isPresent(), is(true));
    assertThat(selector.selectReplica().get().getEndpoint(), is(ENDPOINT_URI_0));

  }


  @Test
  void testDoubleEndpointList() {

    ReplicaEndpointProperties replica0 = new ReplicaEndpointProperties();
    replica0.setEndpoint(ENDPOINT_URI_0);

    ReplicaEndpointProperties replica1 = new ReplicaEndpointProperties();
    replica1.setEndpoint(ENDPOINT_URI_1);


    config.getRedirector().getPool().getEndpoints().add(replica0);
    config.getRedirector().getPool().getEndpoints().add(replica1);

    Set<ReplicaEndpointProperties> results = new HashSet<>();

    // FIXME: this 10 index is completely arbitrary
    for (int i = 0; i < 10; i++) {
      results.add(selector.selectReplica().orElseThrow(assertionError("Replica selection failed")));
    }

    assertThat(results, hasItem(replica0));
    assertThat(results, hasItem(replica1));
  }


}
