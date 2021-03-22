/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.redirector;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.RedirectorProperties.ReplicaEndpointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "storm.redirector.enabled", havingValue = "true")
public class RandomReplicaSelector implements ReplicaSelector {

  public static final Logger LOG = LoggerFactory.getLogger(RandomReplicaSelector.class);

  final List<ReplicaEndpointProperties> endpoints;

  private final Random rng = new Random();

  @Autowired
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
