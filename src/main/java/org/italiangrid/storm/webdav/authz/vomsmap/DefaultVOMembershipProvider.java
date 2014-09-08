package org.italiangrid.storm.webdav.authz.vomsmap;

import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.util.Assert;

public class DefaultVOMembershipProvider implements VOMembershipProvider, Refreshable {

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
