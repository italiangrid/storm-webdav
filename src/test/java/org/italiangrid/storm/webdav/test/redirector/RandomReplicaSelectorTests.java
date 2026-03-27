// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.redirector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.RedirectorProperties.ReplicaEndpointProperties;
import org.italiangrid.storm.webdav.redirector.RandomReplicaSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    assertFalse(selector.selectReplica().isPresent());
  }

  @Test
  void testSingleEndpointList() {

    ReplicaEndpointProperties replica = new ReplicaEndpointProperties(ENDPOINT_URI_0);

    config.getRedirector().getPool().getEndpoints().add(replica);

    assertTrue(selector.selectReplica().isPresent());
    assertEquals(ENDPOINT_URI_0, selector.selectReplica().get().endpoint());
  }

  @Test
  void testDoubleEndpointList() {

    ReplicaEndpointProperties replica0 = new ReplicaEndpointProperties(ENDPOINT_URI_0);

    ReplicaEndpointProperties replica1 = new ReplicaEndpointProperties(ENDPOINT_URI_1);

    config.getRedirector().getPool().getEndpoints().add(replica0);
    config.getRedirector().getPool().getEndpoints().add(replica1);

    Set<ReplicaEndpointProperties> results = new HashSet<>();

    // FIXME: this 10 index is completely arbitrary
    for (int i = 0; i < 10; i++) {
      results.add(selector.selectReplica().orElseThrow(assertionError("Replica selection failed")));
    }

    assertTrue(results.contains(replica0));
    assertTrue(results.contains(replica1));
  }
}
