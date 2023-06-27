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
package org.italiangrid.storm.webdav.authz.vomap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class DefaultVOMapDetailsService implements VOMapDetailsService {

  private static final Logger logger = LoggerFactory
    .getLogger(DefaultVOMapDetailsService.class);

  private final ScheduledExecutorService scheduler = Executors
    .newSingleThreadScheduledExecutor();

  private final long refreshPeriodInSeconds;

  Set<VOMembershipProvider> providers;

  public DefaultVOMapDetailsService(Set<VOMembershipProvider> providers,
    long refreshPeriod) {

    Assert.notNull(providers,
      "Please provide a non-null (but possibly empty) set of providers");
    this.providers = providers;
    this.refreshPeriodInSeconds = refreshPeriod;
    if (refreshPeriodInSeconds > 0) {
      scheduleRefresh();
    }
  }

  private void scheduleRefresh() {

    Runnable refreshTask = new Runnable() {

      @Override
      public void run() {

        refresh();

      }
    };

    scheduler.scheduleWithFixedDelay(refreshTask, 
      refreshPeriodInSeconds, 
      refreshPeriodInSeconds,
      TimeUnit.SECONDS);

  }

  @Override
  public Set<String> getPrincipalVOs(X500Principal principal) {

    Assert.notNull(principal, "PrincipalProperties cannot be null");

    HashSet<String> voNames = new HashSet<String>();

    for (VOMembershipProvider p : providers) {

      if (p.hasSubjectAsMember(principal.getName())) {
        voNames.add(p.getVOName());
      }
    }

    return Collections.unmodifiableSet(voNames);
  }

  protected void refresh() {

    logger.debug("Refreshing vo membership providers...");
    for (VOMembershipProvider p : providers) {
      if (p instanceof Refreshable) {
        try {
          ((Refreshable) p).refresh();
        } catch (Throwable t) {
          logger.warn(
            "Exception caught refreshing VOMembership provider for VO: {}. {}",
            p.getVOName(), t.getMessage(), t);
        }
      }
    }
  }
}
