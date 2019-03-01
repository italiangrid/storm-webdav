/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.util.Assert;

public class DefaultVOMembershipProvider implements VOMembershipProvider,
  Refreshable {

  private final String voName;
  private final VOMembershipSource membershipSource;
  private long lastRefreshTimestamp = 0L;

  protected ReentrantReadWriteLock refreshLock = new ReentrantReadWriteLock();

  protected Set<String> members = null;

  public DefaultVOMembershipProvider(String voName,
    VOMembershipSource membershipSource) {

    Assert.hasText(voName, "voName cannot be null or empty!");
    Assert.notNull(membershipSource, "membershipSource cannot be null!");

    this.voName = voName;
    this.membershipSource = membershipSource;
    members = this.membershipSource.getVOMembers();
  }

  @Override
  public String getVOName() {

    return voName;
  }

  @Override
  public boolean hasSubjectAsMember(String subject) {

    refreshLock.readLock().lock();

    try {
      return members.contains(subject);

    } finally {
      refreshLock.readLock().unlock();
      ;
    }
  }

  @Override
  public void refresh() {

    Set<String> newMembers = membershipSource.getVOMembers();

    refreshLock.writeLock().lock();

    lastRefreshTimestamp = System.currentTimeMillis();

    try {
      members = newMembers;

    } finally {
      refreshLock.writeLock().unlock();
    }
  }

  @Override
  public long getLastRefreshTime() {

    return lastRefreshTimestamp;
  }

}
