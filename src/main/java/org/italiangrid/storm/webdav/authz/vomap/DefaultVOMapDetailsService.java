// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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

  private static final Logger logger = LoggerFactory.getLogger(DefaultVOMapDetailsService.class);

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  private final long refreshPeriodInSeconds;

  Set<VOMembershipProvider> providers;

  public DefaultVOMapDetailsService(Set<VOMembershipProvider> providers, long refreshPeriod) {

    Assert.notNull(providers, "Please provide a non-null (but possibly empty) set of providers");
    this.providers = providers;
    this.refreshPeriodInSeconds = refreshPeriod;
    if (refreshPeriodInSeconds > 0) {
      scheduleRefresh();
    }
  }

  private void scheduleRefresh() {

    Runnable refreshTask = this::refresh;

    scheduler.scheduleWithFixedDelay(
        refreshTask, refreshPeriodInSeconds, refreshPeriodInSeconds, TimeUnit.SECONDS);
  }

  @Override
  public Set<String> getPrincipalVOs(X500Principal principal) {

    Assert.notNull(principal, "PrincipalProperties cannot be null");

    HashSet<String> voNames = new HashSet<>();

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
      if (p instanceof Refreshable refreshable) {
        try {
          refreshable.refresh();
        } catch (Throwable t) {
          logger.warn(
              "Exception caught refreshing VOMembership provider for VO: {}. {}",
              p.getVOName(),
              t.getMessage(),
              t);
        }
      }
    }
  }
}
