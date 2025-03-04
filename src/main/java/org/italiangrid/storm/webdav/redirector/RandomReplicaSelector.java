// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.redirector;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.RedirectorProperties.ReplicaEndpointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "storm.redirector.enabled", havingValue = "true")
public class RandomReplicaSelector implements ReplicaSelector {

  public static final Logger LOG = LoggerFactory.getLogger(RandomReplicaSelector.class);

  final List<ReplicaEndpointProperties> endpoints;

  private final SecureRandom rng = new SecureRandom();

  public RandomReplicaSelector(ServiceConfigurationProperties config) {
    this.endpoints = config.getRedirector().getPool().getEndpoints();
  }

  @Override
  public Optional<ReplicaEndpointProperties> selectReplica() {

    if (endpoints.isEmpty()) {
      LOG.debug("Empty list of endpoints, returning Optional.empty");
      return Optional.empty();
    }

    if (endpoints.size() == 1) {
      return Optional.of(endpoints.get(0));
    }

    int index = rng.nextInt(endpoints.size());
    ReplicaEndpointProperties selected = endpoints.get(index);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Selected endpoint at index {}: {}", index, selected);
    }
    return Optional.ofNullable(selected);
  }

}
