// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.redirector;

import java.util.Optional;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties.RedirectorProperties.ReplicaEndpointProperties;

public interface ReplicaSelector {

  Optional<ReplicaEndpointProperties> selectReplica();
}
